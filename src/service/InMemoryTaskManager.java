package service;

import model.*;

import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    private int generatorId = 0;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private HistoryManager historyManager;

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
        int id = ++generatorId;
        Task createdTask = new Task(id, task.getName(), task.getDescription(), task.getStatus());
        task.setId(id);
        tasks.put(createdTask.getId(), createdTask);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = ++generatorId;
        Epic createdEpic = new Epic(id, epic.getName(), epic.getDescription(), epic.getStatus());
        epic.setId(id);
        epics.put(createdEpic.getId(), createdEpic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        int id = ++generatorId;
        int epicId = subtask.getEpicId();
        Subtask createdSubtask = new Subtask(id, subtask.getName(), subtask.getDescription(), subtask.getStatus(), epicId);
        subtask.setId(id);
        subtasks.put(createdSubtask.getId(), createdSubtask);
        epicCheckStatus(epicId);
        return subtask;
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epicCheckStatus(epic.getId());
        }
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task updateTask(int taskId, Task modifiedTask) {
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
        modifiedSubtask.setId(subtaskId);
        subtasks.put(subtaskId, modifiedSubtask);
        for (Map.Entry<Integer, Epic> epicEntry : epics.entrySet()) {
            if (epicEntry.getValue().getId() == modifiedSubtask.getEpicId()) {
                Epic epic = epicEntry.getValue();
                epicCheckStatus(epic.getId());
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
        return tasks.remove(taskId);
    }

    @Override
    public Epic deleteEpic(int epicId) {
        subtasks.values().removeIf(subtask -> subtask.getEpicId() == epicId);
        return epics.remove(epicId);
    }

    @Override
    public Subtask deleteSubtask(int subtaskId) {
        int epicId = subtasks.get(subtaskId).getEpicId();
        Subtask removeSubtask = subtasks.remove(subtaskId);
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
}