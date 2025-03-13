package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.Optional;

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

    Optional<Task> getTaskById(Integer taskId);

    Optional<Epic> getEpicById(Integer epicId);

    Optional<Subtask> getSubtaskById(Integer subtaskId);

    List<Subtask> getSubtasksByEpicId(int epicId);

    Task deleteTask(int taskId);

    Epic deleteEpic(int epicId);

    Subtask deleteSubtask(int subtaskId);

    void epicCheckStatus(int epicId);

    List<Task> getPrioritizedTasks();

    boolean validateTask(Task task);
}