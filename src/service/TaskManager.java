package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TaskManager {

    private int generatorId = 0;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();


    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public Task createTask(Task task) {
        task.setId(generatorId);
        tasks.put(generatorId, task);
        generatorId++;
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(generatorId);
        epics.put(generatorId, epic);
        generatorId++;
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(generatorId);
        subtasks.put(generatorId, subtask);
        generatorId++;
        return subtask;
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epicCheckStatus(epic.getId());
        }
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Task updateTask(int taskId, Task modifiedTask) {
        modifiedTask.setId(taskId);
        tasks.put(taskId, modifiedTask);
        return modifiedTask;
    }

    public Epic updateEpic(int epicId, Epic modifiedEpic) {
        modifiedEpic.setId(epicId);
        epics.put(epicId, modifiedEpic);
        return modifiedEpic;
    }

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

    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpicById(int epicId) {
        return epics.get(epicId);
    }

    public Subtask getSubtaskById(int subtaskId) {
        return subtasks.get(subtaskId);
    }

    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> subtasksByEpicId = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                subtasksByEpicId.add(subtask);
            }
        }
        return subtasksByEpicId;
    }


    public Task deleteTask(int taskId) {
        return tasks.remove(taskId);
    }

    public Epic deleteEpic(int epicId) {
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getId() == epicId) subtasks.remove(subtask.getId());
        }
        return epics.remove(epicId);
    }

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

    public void epicCheckStatus(int epicId) {
        Epic epic = epics.get(epicId);
        int countSubtasksFromEpic = 0;
        int newStat = 0;
        int doneStat = 0;
        for (Map.Entry<Integer, Subtask> subtaskEntry : subtasks.entrySet()) {
            if (subtaskEntry.getValue().getEpicId() == epicId) {
                countSubtasksFromEpic++;
                if (subtaskEntry.getValue().getStatus() == TaskStatus.IN_PROGRESS) {
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                    return;
                }
                if (subtaskEntry.getValue().getStatus() == TaskStatus.NEW) newStat++;
                else if (subtaskEntry.getValue().getStatus() == TaskStatus.DONE) doneStat++;
            }
        }
        if (newStat == countSubtasksFromEpic) epic.setStatus(TaskStatus.NEW);
        else if ((doneStat == countSubtasksFromEpic)) epic.setStatus(TaskStatus.DONE);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }
}
