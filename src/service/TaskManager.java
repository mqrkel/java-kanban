package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getAllTasksInHistoryList();

    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    Task updateTask(int taskId, Task modifiedTask);

    Epic updateEpic(int epicId, Epic modifiedEpic);

    Subtask updateSubtask(int subtaskId, Subtask modifiedSubtask);

    Task getTaskById(Integer taskId);

    Epic getEpicById(Integer epicId);

    Subtask getSubtaskById(Integer subtaskId);

    List<Subtask> getSubtasksByEpicId(int epicId);

    Task deleteTask(int taskId);

    Epic deleteEpic(int epicId);

    Subtask deleteSubtask(int subtaskId);

    void epicCheckStatus(int epicId);
}