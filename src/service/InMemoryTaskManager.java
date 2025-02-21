package service;

import exceptions.InvalidTaskTimeException;
import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    protected int generatorId = 0;

    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }


    public List<Task> getAllTasksInHistoryList() {
        return historyManager.getHistory();
    }

    @Override
    public Optional<Task> getTaskById(Integer taskId) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            Task taskForHistory = new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus(), task.getDuration(), task.getStartTime());
            historyManager.addToHistory(taskForHistory);
            return Optional.of(task);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpicById(Integer epicId) {
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            Epic taskForHistory = new Epic(epic.getId(), epic.getName(), epic.getDescription(), epic.getStatus(), epic.getDuration(), epic.getStartTime());
            historyManager.addToHistory(taskForHistory);
            return Optional.of(epic);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubtaskById(Integer subtaskId) {
        if (subtasks.containsKey(subtaskId)) {
            Subtask subtask = subtasks.get(subtaskId);
            Subtask taskForHistory = new Subtask(subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
            historyManager.addToHistory(taskForHistory);
            return Optional.of(subtask);
        }
        return Optional.empty();
    }

    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
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
        validateTask(task);
        int id = ++generatorId;
        Task createdTask = new Task(id, task.getName(), task.getDescription(), task.getStatus(), task.getDuration(), task.getStartTime());
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
        Epic createdEpic = new Epic(id, epic.getName(), epic.getDescription(), epic.getStatus(), epic.getDuration(), epic.getStartTime());
        updateEpicTime(createdEpic);
        epic.setId(id);
        epics.put(createdEpic.getId(), createdEpic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        validateTask(subtask);
        int id = ++generatorId;
        int epicId = subtask.getEpicId();
        Subtask createdSubtask = new Subtask(id, subtask.getName(), subtask.getDescription(), subtask.getStatus(), epicId, subtask.getDuration(), subtask.getStartTime());
        Epic epic = epics.get(epicId);
        epic.addSubtaskId(id);
        subtask.setId(id);
        subtasks.put(createdSubtask.getId(), createdSubtask);
        updateEpicTime(epic);
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
        removeSubtasksFromSortedList();
        removeEpicsFromHistory();
        removeSubtasksFromHistory();
        epics.clear();
        subtasks.clear();
    }

    private void removeSubtasksFromSortedList() {
        prioritizedTasks.removeIf(task -> task.getTaskType().equals(TaskType.SUBTASK));
    }


    @Override
    public Task updateTask(int taskId, Task modifiedTask) {
        validateTask(modifiedTask);
        Optional<Task> oldTask = getTaskById(taskId);
        oldTask.ifPresent(prioritizedTasks::remove);
        modifiedTask.setId(taskId);
        tasks.put(taskId, modifiedTask);
        if (modifiedTask.getStartTime() != null) prioritizedTasks.add(modifiedTask);
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
        validateTask(modifiedSubtask);
        Optional<Subtask> oldSubtask = getSubtaskById(subtaskId);
        oldSubtask.ifPresent(prioritizedTasks::remove);
        modifiedSubtask.setId(subtaskId);
        subtasks.put(subtaskId, modifiedSubtask);
        Epic epic = epics.get(modifiedSubtask.getEpicId());
        epicCheckStatus(epic.getId());
        updateEpicTime(epic);
        prioritizedTasks.add(modifiedSubtask);
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
                prioritizedTasks.remove(subtaskEntry.getValue());
                iterator.remove();
                historyManager.removeFromTaskHistory(subtaskEntry.getValue().getId());
            }
        }
        historyManager.removeFromTaskHistory(epicId);
        return epics.remove(epicId);
    }

    @Override
    public Subtask deleteSubtask(int subtaskId) {
        Optional<Subtask> subtaskOptional = getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) return null;
        Subtask removeSubtask = subtasks.remove(subtaskId);
        prioritizedTasks.remove(removeSubtask);
        historyManager.removeFromTaskHistory(subtaskId);
        int epicId = removeSubtask.getEpicId();
        Optional<Epic> epicOptional = getEpicById(epicId);
        epicOptional.ifPresent(epic -> {
            epic.removeSubtaskId(subtaskId);
            epicCheckStatus(epicId);
            updateEpicTime(epic);
        });
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
                if (subtask.getStatus() == TaskStatus.NEW) newStat++;
                else if (subtask.getStatus() == TaskStatus.DONE) doneStat++;
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
        List<Integer> subs = epic.getSubtaskIdsByEpic();
        if (subs.isEmpty()) {
            return;
        }
        LocalDateTime minStartTime = LocalDateTime.MAX;
        LocalDateTime maxEndTime = LocalDateTime.MIN;
        long duration = 0L;
        for (Integer id : subs) {
            Subtask subtask = subtasks.get(id);
            if (subtask.getStartTime() == null) {
                continue;
            }
            LocalDateTime startTime = subtask.getStartTime();
            LocalDateTime endTime = subtask.getEndTime();
            if (startTime.isBefore(minStartTime))
                minStartTime = startTime;
            if (endTime.isAfter(maxEndTime)) {
                maxEndTime = endTime;
            }
            duration += subtask.getDuration().toMinutes();
        }
        if (minStartTime == LocalDateTime.MAX) minStartTime = null;
        if (maxEndTime == LocalDateTime.MIN) maxEndTime = null;
        epic.setStartTime(minStartTime);
        epic.setEndTime(maxEndTime);
        epic.setDuration(Duration.ofMinutes(duration));
    }

    private void validateTask(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return;
        }
        List<Integer> conflictingIds = findConflictingTasks(task);
        if (!conflictingIds.isEmpty()) {
            throw new InvalidTaskTimeException("Задача с id=" + task.getId() + " пересекается с задачами id=" + conflictingIds);
        }
    }

    private List<Integer> findConflictingTasks(Task task) {
        List<Integer> conflictingIds = new ArrayList<>();
        LocalDateTime taskStart = task.getStartTime();
        LocalDateTime taskEnd = task.getEndTime();
        for (Task t : prioritizedTasks) {
            if (t.getId() == task.getId() || t.getStartTime() == null || t.getEndTime() == null) {
                continue;
            }
            LocalDateTime otherStart = t.getStartTime();
            LocalDateTime otherEnd = t.getEndTime();
            if (taskStart.isBefore(otherEnd) && taskEnd.isAfter(otherStart)) {
                conflictingIds.add(t.getId());
            }
        }
        return conflictingIds;
    }
}