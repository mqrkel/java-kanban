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

    public static Map<Integer, Task> taskAndEpicMap = new HashMap<>();

    public List<String> getAllTasks() {
        List<String> list = new ArrayList<>();
        for (Task task : taskAndEpicMap.values()) {
            list.add(task.toString());
        }
        return list;
    }

    public void changeTaskStatusInProgress(int taskId) {
        Task task = taskAndEpicMap.get(taskId);
        if (task.getClass() != Epic.class) task.setStatus(TaskStatus.IN_PROGRESS);
        else System.out.println("Вы не можете поменять статус у эпика");
    }

    public void changeTaskStatusToDone(int taskId) {
        Task task = taskAndEpicMap.get(taskId);
        if (task.getClass() != Epic.class) task.setStatus(TaskStatus.DONE);
    }

    public void clearAllTasks() {
        taskAndEpicMap.clear();
    }

    public Task getById(int id) {
        return taskAndEpicMap.get(id);
    }

    public Task createTask(String name, String description) {
        Task task = new Task(name, description);
        taskAndEpicMap.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(String name, String description) {
        Epic epic = new Epic(name, description);
        taskAndEpicMap.put(epic.getId(), epic);
        return epic;
    }

    public void updateTask(int oldId, String newName, String newDescription) {
        if (taskAndEpicMap.get(oldId).getClass() == Epic.class) {
            taskAndEpicMap.remove(oldId);
            Epic epic = createEpic(newName, newDescription);
            taskAndEpicMap.put(epic.getId(), epic);
        } else {
            taskAndEpicMap.remove(oldId);
            Task task = createTask(newName, newDescription);
            taskAndEpicMap.put(task.getId(), task);
        }
    }

    public void deleteTask(int id) {
        taskAndEpicMap.remove(id);
    }

    public void deleteSubtask(int epicId, String name) {
        Task task = taskAndEpicMap.get(epicId);
        if (task.getClass() == Epic.class) {
            Epic epic = (Epic) task;
            epic.getSubtasks().removeIf(subtask -> subtask.getName().equals(name));
            epic.checkStatus();
        }
    }

    public List<Subtask> getSubtasksFromEpicId(int epicId) {
        Epic epic = (Epic) getById(epicId);
        return new ArrayList<>(epic.getSubtasks());
    }

    public Subtask createSubtask(int epicId, String name) {
        Task task = taskAndEpicMap.get(epicId);
        if (task.getClass() == Epic.class) {
            Subtask subtask = new Subtask(name);
            Epic epic = (Epic) task;
            epic.getSubtasks().add(subtask);
            epic.checkStatus();
            return subtask;
        }
        return null;
    }

    public void changeSubtaskToDone(int epicId, String subtaskName) {
        Epic epic = (Epic) TaskManager.taskAndEpicMap.get(epicId);
        for (Subtask subtask : epic.getSubtasks()) {
            if (subtask.getName().equals(subtaskName)) subtask.setStatus(TaskStatus.DONE);
        }
        epic.checkStatus();
    }

    public void changeSubtaskToInProgress(int epicId, String subtaskName) {
        Epic epic = (Epic) TaskManager.taskAndEpicMap.get(epicId);
        for (Subtask subtask : epic.getSubtasks()) {
            if (subtask.getName().equals(subtaskName)) epic.setStatus(TaskStatus.IN_PROGRESS);

        }
        epic.checkStatus();
    }
}
