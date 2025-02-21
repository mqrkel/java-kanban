package service;


import exceptions.InvalidTaskTimeException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    public void init() {
        taskManager = createTaskManager();
    }

    @Test
    void testManagerInitialization() {
        assertNotNull(taskManager);
    }

    @Test
    void addTasksOfDifferentTypesAndCanFindThemById() {
        Task task = taskManager.createTask(new Task("Наименование задачи", "Описание задачи"));
        Epic epic = taskManager.createEpic(new Epic("Наименование эпика", "Описание эпика"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Наименование сабтаска", epic.getId(), "Описание сабтаска", TaskStatus.NEW));
        Optional<Task> taskById = taskManager.getTaskById(task.getId());
        Optional<Epic> epicById = taskManager.getEpicById(epic.getId());
        Optional<Subtask> subtaskById = taskManager.getSubtaskById(subtask.getId());

        assertTrue(taskById.isPresent(), "Задача не найдена");
        assertTrue(epicById.isPresent(), "Эпик не найден");
        assertTrue(subtaskById.isPresent(), "Подзадача не найдена");
        assertEquals(taskById.get(), task, "Задачи не совпадают");
        assertEquals(epicById.get(), epic, "Эпики не совпадают");
        assertEquals(subtaskById.get(), subtask, "Подзадачи не совпадают");
    }

    @Test
    void addTasksToHistoryAndAddingTaskAgainShouldRewriteTaskToTheEndOfTheList() {
        String name = "Наименование ";
        String description = "Описание ";
        List<Task> evenNumbers = new ArrayList<>();

        for (int i = 1; i < 16; i++) {
            Task task = new Task(name + i, description + i);
            taskManager.createTask(task);
            Optional<Task> taskById = taskManager.getTaskById(task.getId());
            assertTrue(taskById.isPresent(), "Задача с ID " + task.getId() + " не найдена");
            if (i % 2 == 0) {
                evenNumbers.add(taskById.get());
            }
        }
        for (Task evenNumber : evenNumbers) {
            taskManager.getTaskById(evenNumber.getId());
        }
        assertEquals(15, taskManager.getAllTasksInHistoryList().size(), "Количество задач в списке должно быть равно 15");

        Optional<Task> task = taskManager.getTaskById(14);
        assertTrue(task.isPresent(), "Задача с ID 14 не найдена");
        List<Task> history = taskManager.getAllTasksInHistoryList();
        assertEquals(task.get(), history.getLast(), "Задача с ID 14 должна быть в конце списка.");
    }

    @Test
    void addTaskInHistoryList() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";

        Task task = taskManager.createTask(new Task(name, description));
        taskManager.getTaskById(task.getId());
        Task taskFromHistory = taskManager.getAllTasksInHistoryList().getFirst();

        assertEquals(task, taskFromHistory, "Задача не добавилась в список историй");
    }

    @Test
    void testTaskWithSameIdAreEquals() {
        Task task1 = new Task(1, "Наименование 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task(1, "Наименование 2", "Описание 2", TaskStatus.DONE);
        assertEquals(task1, task2, "Задачи должны быть равны по их id");
    }

    @Test
    void testEpicWithSameIdAreEquals() {
        Epic epic1 = new Epic(1, "Наименование 1", "Описание 1", TaskStatus.NEW);
        Epic epic2 = new Epic(1, "Наименование 2", "Описание 2", TaskStatus.DONE);
        assertEquals(epic1, epic2, "Эпики должны быть равны по их id");
    }

    @Test
    void testSubtaskWithSameIdAreEquals() {
        Subtask subtask1 = new Subtask(1, "Наименование 1", "Описание 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(1, "Наименование 2", "Описание 2", TaskStatus.DONE, 0);
        assertEquals(subtask1, subtask2, "Подзадачи должны быть равны по их id");
    }

    @Test
    void getTasks() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            taskManager.createTask(new Task(name + i, description + i));
        }
        assertEquals(5, taskManager.getTasks().size(), "Количество задач в списке должно быть равным 5");
    }

    @Test
    void getSubtasks() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            taskManager.createEpic(new Epic(name + i, description + i));
        }
        assertEquals(5, taskManager.getEpics().size(), "Количество подзадач в списке должно быть равным 5");
    }

    @Test
    void getEpics() {
        String name = "Наименование ";
        String description = "Описание ";
        Epic epic = new Epic(name + 1, description + 1);
        taskManager.createEpic(epic);
        Optional<Epic> actualEpic = taskManager.getEpicById(epic.getId());
        assertTrue(actualEpic.isPresent(), "Эпик с ID=" + actualEpic + "не найден.");
        for (int i = 2; i < 7; i++) {
            taskManager.createSubtask(new Subtask(name + i, actualEpic.get().getId(), description + i, TaskStatus.NEW));
        }
        assertEquals(5, taskManager.getSubtasks().size(), "Количество эпиков в списке должно быть равным 5");
    }

    @Test
    void idsByTasksShouldBeUnique() {
        String name = "Наименование задачи ";
        String description = "Описание задачи ";

        Task task = new Task(name + "1", description + "1");
        Task createdTask = taskManager.createTask(task);
        for (int i = 2; i < 7; i++) {
            taskManager.createTask(new Task(name + i, description + i));
        }
        Task createdTaskByFilledId = taskManager.createTask(new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()));

        assertEquals(7, taskManager.getTasks().size(), "Количество задач в списке должно быть равным 7");
        assertNotEquals(createdTask.getId(), createdTaskByFilledId.getId(), "Задачи с заданным id и сгенерированным id" + " не должны конфликтовать внутри менеджера");
    }


    @Test
    void createTask() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";
        Task task = new Task(name, description);

        taskManager.createTask(task);
        Optional<Task> actualTask = taskManager.getTaskById(task.getId());
        assertTrue(actualTask.isPresent(), "Задача с ID=" + task.getId() + " не найдена");
        assertEquals(task.getId(), actualTask.get().getId(), "ID задачи не совпадает");
        assertEquals(TaskStatus.NEW, actualTask.get().getStatus(), "Неверно установлен статус задачи");
        assertEquals(description, actualTask.get().getDescription(), "Неверно установлено описание задачи");
        assertEquals(name, actualTask.get().getName(), "Неверно установлено наименование задачи");
    }

    @Test
    void createEpic() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";
        Epic epic = new Epic(name, description);

        taskManager.createEpic(epic);
        Optional<Epic> actualEpic = taskManager.getEpicById(epic.getId());
        assertTrue(actualEpic.isPresent(), "Задача с ID=" + epic.getId() + " не найдена");
        assertEquals(epic.getId(), actualEpic.get().getId(), "ID задачи не совпадает");
        assertEquals(TaskStatus.NEW, actualEpic.get().getStatus(), "Неверно установлен статус эпика");
        assertEquals(description, actualEpic.get().getDescription(), "Неверно установлено описание эпика");
        assertEquals(name, actualEpic.get().getName(), "Неверно установлено наименование задачи");
    }

    @Test
    void createSubtask() {
        String subtaskName = "Наименование подзадачи 1";
        String subtaskDescription = "Описание подзадачи 1";
        String epicName = "Название эпика 1";
        String epicDescription = "Описание эпика 1";

        Epic epic = new Epic(epicName, epicDescription);
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(subtaskName, epic.getId(), subtaskDescription, TaskStatus.NEW);

        taskManager.createSubtask(subtask);
        Optional<Subtask> actualSubtask = taskManager.getSubtaskById(subtask.getId());
        assertTrue(actualSubtask.isPresent(), "Задача с ID=" + subtask.getId() + " не найдена");
        assertEquals(subtask.getId(), actualSubtask.get().getId(), "ID подзадачи не совпадает");
        assertEquals(TaskStatus.NEW, actualSubtask.get().getStatus(), "Неверно установлен статус подзадачи");
        assertEquals(subtaskName, actualSubtask.get().getName(), "Неверно установлено наименование подзадачи");
        assertEquals(subtaskDescription, actualSubtask.get().getDescription(), "Неверно установлено описание подзадачи");
    }

    @Test
    void removingEpicShouldRemoveAllSubtasks() {
        String name = "Наименование подзадачи ";
        String description = "Описание подзадачи ";
        Epic epic = taskManager.createEpic(new Epic("Наименование эпика", "Описание эпика"));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask(name + i, epic.getId(), description + i, TaskStatus.NEW));
        }
        taskManager.deleteEpic(epic.getId());
        assertTrue(taskManager.getEpics().isEmpty(), "Эпик остался в списке после удаления");
        assertTrue(taskManager.getSubtasks().isEmpty(), "При удалении эпика необходимо удалить его подзадачи");
    }

    @Test
    void ifSubtasksHaveDoneStatusThenEpicShouldHaveDoneStatus() {
        Epic epic = taskManager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.DONE));
        }

        Optional<Epic> updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.isPresent(), "Эпик с ID=" + epic.getId() + " не найден");
        assertEquals(TaskStatus.DONE, updatedEpic.get().getStatus(), "Эпик должен иметь статус 'DONE', т.к. все его подзадачи завершены");
    }

    @Test
    void ifSubtasksHaveNewStatusThenEpicShouldHaveNewStatus() {
        Epic epic = taskManager.createEpic(new Epic("Наименование", "Описание"));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.NEW));
        }
        Optional<Epic> updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.isPresent(), "Эпик с ID=" + epic.getId() + " не найден");
        assertEquals(TaskStatus.NEW, updatedEpic.get().getStatus(), "Эпик должен иметь статус 'NEW', т.к. все его подзадачи 'NEW'");
    }

    @Test
    void ifSubtasksHaveDifferentStatusesThenEpicShouldHaveStatusOfInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Наименование", "Описание"));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.NEW));
        }
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.DONE));
        }
        Optional<Epic> updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.isPresent(), "Эпик с ID=" + epic.getId() + " не найден");
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.get().getStatus(), "Эпик должен иметь статус 'IN_PROGRESS', так как его подзадачи имеют разные статусы");
    }

    @Test
    void taskInHistoryListShouldNotUpdateAfterTaskUpdate() {
        String name = "Наименование задачи 1";
        String modifiedName = "Измененное наименование задачи 1";
        String description = "Описание задачи 1";
        String modifiedDescription = "Измененное описание задачи 1";
        Task task = new Task(name, description);
        Task modifiedTask = new Task(modifiedName, modifiedDescription, TaskStatus.DONE);

        Task createdTask = taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        Task taskInHistory = taskManager.getAllTasksInHistoryList().getFirst();

        TaskStatus statusInHistoryBeforeUpdate = taskInHistory.getStatus();
        String descriptionInHistoryBeforeUpdate = taskInHistory.getDescription();
        String nameInHistoryBeforeUpdate = taskInHistory.getName();

        taskManager.updateTask(createdTask.getId(), modifiedTask);

        Task taskInHistoryAfterUpdate = taskManager.getAllTasksInHistoryList().getFirst();
        assertEquals(statusInHistoryBeforeUpdate, taskInHistoryAfterUpdate.getStatus(), "Статус задачи должен остаться прежним в истории");
        assertEquals(descriptionInHistoryBeforeUpdate, taskInHistoryAfterUpdate.getDescription(), "Описание задачи должно остаться прежним");
        assertEquals(nameInHistoryBeforeUpdate, taskInHistoryAfterUpdate.getName(), "Наименование задачи должно остаться прежним");
    }

    @Test
    void WhenRemoveTaskFromTasksListAlsoTaskShouldBeDeletedFromHistoryList() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            Epic epic = new Epic(name + i, description + i);
            taskManager.createEpic(epic);
            taskManager.getEpicById((epic.getId()));
        }
        for (int i = 0; i < 5; i++) {
            Task task = new Task(name + i, description + i);
            taskManager.createTask(task);
            taskManager.getTaskById((task.getId()));
        }
        taskManager.deleteTasks();
        assertEquals(5, taskManager.getAllTasksInHistoryList().size(), "При удалении задачи, список в истории должен быть уменьшен на 5");
    }

    @Test
    void WhenRemoveEpicFromTasksListAlsoEpicShouldBeDeletedFromHistoryList() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            Epic epic = new Epic(name + i, description + i);
            taskManager.createEpic(epic);
            taskManager.getEpicById((epic.getId()));
        }
        taskManager.deleteEpic(1);
        assertEquals(4, taskManager.getAllTasksInHistoryList().size(), "При удалении задачи, список в истории должен быть уменьшен на 1");
    }

    @Test
    void WhenRemoveSubtaskFromTasksListAlsoSubtaskShouldBeDeletedFromHistoryList() {
        String name = "Наименование ";
        String description = "Описание ";
        Epic epic = taskManager.createEpic(new Epic(name + 1, description + 1));
        for (int i = 0; i < 5; i++) {
            Subtask subtask = new Subtask(name + i, description + i, epic.getId());
            taskManager.createSubtask(subtask);
            taskManager.getSubtaskById((subtask.getId()));
        }
        taskManager.deleteSubtask(2);
        assertEquals(5, taskManager.getAllTasksInHistoryList().size(), "При удалении задачи, список в истории должен быть уменьшен на 1");
    }

    @Test
    void shouldAddTasksToPrioritizedTasksInCorrectOrder() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 10, 0));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW, Duration.ofMinutes(45), LocalDateTime.of(2025, 2, 20, 9, 0));
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.NEW, Duration.ofMinutes(15), LocalDateTime.of(2025, 2, 20, 11, 0));

        Task createdTask1 = taskManager.createTask(task1);
        Task createdTask2 = taskManager.createTask(task2);
        Task createdTask3 = taskManager.createTask(task3);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();

        assertEquals(3, sortedTasks.size());
        assertEquals(createdTask2.getName(), sortedTasks.get(0).getName());
        assertEquals(createdTask1.getName(), sortedTasks.get(1).getName());
        assertEquals(createdTask3.getName(), sortedTasks.get(2).getName());
    }

    @Test
    void shouldRemoveTasksFromPrioritizedTasks() {
        Task task = taskManager.createTask(new Task("Task to Remove", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 10, 0)));
        Epic epic = taskManager.createEpic(new Epic("Epic to Remove", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask to Remove", "Desc", epic.getId(), Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 11, 0)));

        assertEquals(2, taskManager.getPrioritizedTasks().size());
        taskManager.deleteTask(task.getId());
        taskManager.deleteSubtask(subtask.getId());
        taskManager.deleteEpic(epic.getId());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Задачи должны быть удалены из отсортированного списка");
    }

    @Test
    void shouldUpdateTaskInPrioritizedTasks() {
        Task task = taskManager.createTask(new Task("Task", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 10, 0)));

        Task updatedTask = new Task("Task", "Updated Desc", TaskStatus.IN_PROGRESS, Duration.ofMinutes(45), LocalDateTime.of(2025, 2, 20, 12, 0));

        taskManager.updateTask(task.getId(), updatedTask);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();

        assertEquals(1, sortedTasks.size());
        assertEquals(updatedTask.getStartTime(), sortedTasks.getFirst().getStartTime());
    }

    @Test
    void shouldAllowAddingTaskAfterRemovingOverlappingOne() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 2, 20, 9, 0)));

        taskManager.deleteTask(task1.getId());

        taskManager.createTask(new Task("Task 2", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 9, 0)));

        assertEquals(1, taskManager.getPrioritizedTasks().size());
        assertEquals("Task 2", taskManager.getPrioritizedTasks().getFirst().getName());
    }

    @Test
    void shouldAllowAddingSubtaskAfterRemovingOverlappingOne() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 2, 20, 9, 0)));

        taskManager.deleteSubtask(subtask.getId());

        taskManager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 2, 20, 9, 0)));

        assertEquals(1, taskManager.getPrioritizedTasks().size());
        assertEquals("Subtask 2", taskManager.getPrioritizedTasks().getFirst().getName());
    }

    @Test
    void shouldAllowTaskStartingExactlyAfterAnother() {
        taskManager.createTask(new Task("Task 1", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 30)));

        Task task = new Task("Task 2", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 25));

        assertDoesNotThrow(() -> taskManager.createTask(task));
    }

    @Test
    void shouldThrowExceptionWhenTasksOverlap() {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 30));
        taskManager.createTask(task);

        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 27));
        Task task3 = new Task("Task 2", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 33));
        Task task4 = new Task("Task 2", "Desc", Duration.ofMinutes(11), LocalDateTime.of(2025, 2, 20, 12, 25));
        Task task5 = new Task("Task 2", "Desc", Duration.ofMinutes(3), LocalDateTime.of(2025, 2, 20, 12, 31));

        assertThrows(InvalidTaskTimeException.class, () -> taskManager.createTask(task2), "Задача пересекается с существующей");
        assertThrows(InvalidTaskTimeException.class, () -> taskManager.createTask(task3), "Задача пересекается с существующей");
        assertThrows(InvalidTaskTimeException.class, () -> taskManager.createTask(task4), "Задача пересекается с существующей");
        assertThrows(InvalidTaskTimeException.class, () -> taskManager.createTask(task5), "Задача пересекается с существующей");
    }

    @Test
    void shouldNotAllowTaskWithoutStartTime() {
        Task task = new Task("Task without time", "Desc", Duration.ZERO, null);

        assertDoesNotThrow(() -> taskManager.createTask(task));

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size());
    }

    @Test
    void shouldClearPrioritizedTasksAfterDeletingAll() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createTask(new Task("Task 1", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 30)));
        taskManager.createTask(new Task("Task 2", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 35)));
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId(), Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 40)));
        taskManager.deleteTasks();
        taskManager.deleteSubtasks();
        assertEquals(0, taskManager.getPrioritizedTasks().size());
    }

    @Test
    void shouldClearPrioritizedTasksAfterDeletingAllEpics() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createTask(new Task("Task 1", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 30)));
        taskManager.createTask(new Task("Task 2", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 35)));
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId(), Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 40)));
        taskManager.deleteEpics();
        assertEquals(2, taskManager.getPrioritizedTasks().size());
    }

    @Test
    void shouldClearPrioritizedTasksAfterDeletingOneEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createTask(new Task("Task 1", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 30)));
        taskManager.createTask(new Task("Task 2", "Desc", Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 35)));
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId(), Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 40)));
        taskManager.deleteEpic(epic.getId());
        assertEquals(2, taskManager.getPrioritizedTasks().size());
    }

    @Test
    void shouldUpdateEpicTimeWhenAddingSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId()));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId(), Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 12, 30)));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId(), Duration.ofMinutes(5), LocalDateTime.of(2025, 2, 20, 13, 30)));
        Optional<Epic> updateEpicOptional = taskManager.getEpicById(epic.getId());
        assertTrue(updateEpicOptional.isPresent());
        Epic updateEpic = updateEpicOptional.get();

        assertEquals(updateEpic.getStartTime(), subtask1.getStartTime());
        assertEquals(updateEpic.getDuration(), subtask1.getDuration().plus(subtask2.getDuration()));
        assertEquals(updateEpic.getEndTime(), subtask2.getEndTime());
    }

    @Test
    void test() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", epic.getId()));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId()));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc", epic.getId()));
        Optional<Epic> updateEpicOptional = taskManager.getEpicById(epic.getId());
        assertTrue(updateEpicOptional.isPresent());
        Epic updateEpic = updateEpicOptional.get();

        assertNull(updateEpic.getStartTime());
        assertEquals(Duration.ZERO, updateEpic.getDuration());
        assertNull(updateEpic.getEndTime());
    }

}
