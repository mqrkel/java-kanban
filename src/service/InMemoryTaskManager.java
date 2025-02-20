package service;

import exceptions.InvalidTaskTimeException;
import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    private int generatorId = 0;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }


    public List<Task> getAllTasksInHistoryList() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTaskById(Integer taskId) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            Task taskForHistory = new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus());
            historyManager.addToHistory(taskForHistory);
            return task;
        }
        return null;
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            Epic taskForHistory = new Epic(epic.getId(), epic.getName(), epic.getDescription(), epic.getStatus());
            historyManager.addToHistory(taskForHistory);
            return epic;
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        if (subtasks.containsKey(subtaskId)) {
            Subtask subtask = subtasks.get(subtaskId);
            Subtask taskForHistory = new Subtask(subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId());
            historyManager.addToHistory(taskForHistory);
            return subtask;
        }
        return null;
    }

    public Set<Task> getPrioritizedTasks() {
        return new TreeSet<>(prioritizedTasks);
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Task createTask(Task task) {
        try {
            validateTask(task);
        } catch (InvalidTaskTimeException e) {
            System.err.println(e.getMessage());
        }
        int id = ++generatorId;
        Task createdTask = new Task(id, task.getName(), task.getDescription(), task.getStatus());
        task.setId(id);
        tasks.put(createdTask.getId(), createdTask);
        if (createdTask.getStartTime() != null) {
            prioritizedTasks.add(createdTask);
        }
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = ++generatorId;
        Epic createdEpic = new Epic(id, epic.getName(), epic.getDescription(), epic.getStatus());
        updateEpicTime(createdEpic);
        epic.setId(id);
        epics.put(createdEpic.getId(), createdEpic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        try {
            validateTask(subtask);
        } catch (InvalidTaskTimeException e) {
            System.err.println(e.getMessage());
        }
        int id = ++generatorId;
        int epicId = subtask.getEpicId();
        Subtask createdSubtask = new Subtask(id, subtask.getName(), subtask.getDescription(), subtask.getStatus(), epicId);
        Epic epic = getEpicById(epicId);
        epic.addSubtaskId(id);
        updateEpicTime(epic);
        subtask.setId(id);
        subtasks.put(createdSubtask.getId(), createdSubtask);
        epicCheckStatus(epicId);
        if (createdSubtask.getStartTime() != null) {
            prioritizedTasks.add(createdSubtask);
        }
        return subtask;
    }

    @Override
    public void deleteTasks() {
        removeTasksFromHistory();
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.clear();
    }


    @Override
    public void deleteSubtasks() {
        clearSubtaskIds();
        subtasks.values().forEach(prioritizedTasks::remove);
        removeSubtasksFromHistory();
        subtasks.clear();
        epics.values().forEach(epic -> {
            epicCheckStatus(epic.getId());
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ofMinutes(0));
        });
    }

    private void clearSubtaskIds() {
        epics.values().forEach(Epic::clearIdsSubtasksByEpic);
    }

    @Override
    public void deleteEpics() {
        removeEpicsFromHistory();
        removeSubtasksFromHistory();
        epics.clear();
        subtasks.clear();
    }


    @Override
    public Task updateTask(int taskId, Task modifiedTask) {
        try {
            validateTask(modifiedTask);
        } catch (InvalidTaskTimeException e) {
            System.err.println(e.getMessage());
        }
        modifiedTask.setId(taskId);
        tasks.put(taskId, modifiedTask);
        return modifiedTask;
    }

    @Override
    public Epic updateEpic(int epicId, Epic modifiedEpic) {
        modifiedEpic.setId(epicId);
        epics.put(epicId, modifiedEpic);
        return modifiedEpic;
    }

    @Override
    public Subtask updateSubtask(int subtaskId, Subtask modifiedSubtask) {
        try {
            validateTask(modifiedSubtask);
        } catch (InvalidTaskTimeException e) {
            System.err.println(e.getMessage());
        }
        modifiedSubtask.setId(subtaskId);
        subtasks.put(subtaskId, modifiedSubtask);
        for (Map.Entry<Integer, Epic> epicEntry : epics.entrySet()) {
            if (epicEntry.getValue().getId() == modifiedSubtask.getEpicId()) {
                Epic epic = epicEntry.getValue();
                epicCheckStatus(epic.getId());
                updateEpicTime(epic);
                prioritizedTasks.add(modifiedSubtask);
            }
        }
        return modifiedSubtask;
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> subtasksByEpicId = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                subtasksByEpicId.add(subtask);
            }
        }
        return subtasksByEpicId;
    }


    @Override
    public Task deleteTask(int taskId) {
        prioritizedTasks.remove(tasks.get(taskId));
        historyManager.removeFromTaskHistory(taskId);
        return tasks.remove(taskId);
    }

    @Override
    public Epic deleteEpic(int epicId) {
        Iterator<Map.Entry<Integer, Subtask>> iterator = subtasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Subtask> subtaskEntry = iterator.next();
            if (subtaskEntry.getValue().getEpicId() == epicId) {
                iterator.remove();
                historyManager.removeFromTaskHistory(subtaskEntry.getValue().getId());
            }
        }
        historyManager.removeFromTaskHistory(epicId);
        return epics.remove(epicId);
    }

    @Override
    public Subtask deleteSubtask(int subtaskId) {
        int epicId = subtasks.get(subtaskId).getEpicId();
        Subtask removeSubtask = subtasks.remove(subtaskId);
        historyManager.removeFromTaskHistory(subtaskId);
        for (Map.Entry<Integer, Epic> epicEntry : epics.entrySet()) {
            if (epicEntry.getKey() == epicId) {
                epicCheckStatus(epicId);
            }
        }
        return removeSubtask;
    }

    @Override
    public void epicCheckStatus(int epicId) {
        Epic epic = epics.get(epicId);
        int countSubtasksFromEpic = 0;
        int newStat = 0;
        int doneStat = 0;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                countSubtasksFromEpic++;
                if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                    return;
                }
                if (subtask.getStatus() == TaskStatus.NEW)
                    newStat++;
                else if (subtask.getStatus() == TaskStatus.DONE)
                    doneStat++;
            }
        }
        if (doneStat == countSubtasksFromEpic) epic.setStatus(TaskStatus.DONE);
        else if (newStat == countSubtasksFromEpic) epic.setStatus(TaskStatus.NEW);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }

    private void removeTasksFromHistory() {
        for (Task task : tasks.values()) {
            if (task != null) {
                historyManager.removeFromTaskHistory(task.getId());
            }
        }
    }

    private void removeEpicsFromHistory() {
        for (Epic epic : epics.values()) {
            if (epic != null) {
                historyManager.removeFromTaskHistory(epic.getId());
            }
        }
    }

    private void removeSubtasksFromHistory() {
        for (Subtask subtask : subtasks.values()) {
            if (subtask != null) {
                historyManager.removeFromTaskHistory(subtask.getId());
            }
        }
    }

    private void updateEpicTime(Epic epic) {
        LocalDateTime startTime = getMinimalDateTime(epic);
        long duration = calculateEpicDuration(epic.getSubtaskIdsByEpic());
        epic.setStartTime(startTime);
        epic.setDuration(Duration.ofMinutes(duration));
        epic.setEndTime(epic.getStartTime().plus(Duration.ofMinutes(duration)));
    }

    private long calculateEpicDuration(List<Integer> subtaskIdsByEpic) {
        return subtaskIdsByEpic.stream()
                .map(subtasks::get)
                .map(subtask -> subtask.getDuration().toMinutes())
                .reduce(0L, Long::sum);
    }

    private LocalDateTime getMinimalDateTime(Epic epic) {
        Optional<LocalDateTime> startTime = epic.getSubtaskIdsByEpic().stream()
                .map(subtasks::get)
                .map(Task::getStartTime)
                .min(Comparator.naturalOrder());
        return startTime.orElse(null);
    }

    private void validateTask(Task task) throws InvalidTaskTimeException {
        List<Integer> collected = prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .filter(t -> (t.getStartTime().isBefore(task.getStartTime()) && (t.getEndTime().isAfter(task.getStartTime()))) ||
                             (t.getStartTime().isBefore(task.getEndTime()) && (t.getEndTime().isAfter(task.getEndTime()))) ||
                             (t.getStartTime().isBefore(task.getStartTime()) && (t.getEndTime().isAfter(task.getEndTime()))) ||
                             (t.getStartTime().isAfter(task.getStartTime()) && (t.getEndTime().isBefore(task.getEndTime()))) ||
                             (t.getStartTime().equals(task.getStartTime())))
                .map(Task::getId)
                .toList();

        if (!collected.isEmpty()) {
            throw new InvalidTaskTimeException("Задача с id=" + task.getId() + " пересекается с задачами id=" + collected);
        }
    }
}