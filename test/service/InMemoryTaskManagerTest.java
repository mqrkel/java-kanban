package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testManagerInitialization() {
        Assertions.assertNotNull(taskManager);
    }

    @Test
    void addTasksOfDifferentTypesAndCanFindThemById() {
        Task task = taskManager.createTask(new Task("Наименование задачи", "Описание задачи"));
        Epic epic = taskManager.createEpic(new Epic("Наименование эпика", "Описание эпика"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Наименование сабтаска", epic.getId(), "Описание сабтаска", TaskStatus.NEW));
        Task taskById = taskManager.getTaskById(task.getId());
        Epic epicById = taskManager.getEpicById(epic.getId());
        Subtask subtaskById = taskManager.getSubtaskById(subtask.getId());
        Assertions.assertEquals(taskById, task, "Задачи не совпадают");
        Assertions.assertEquals(epicById, epic, "Эпики не совпадают");
        Assertions.assertEquals(subtaskById, subtask, "Подзадачи не совпадают");
    }

    @Test
    void add15TasksToHistoryListAndShouldGet10() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 1; i < 16; i++) {
            Task task = new Task(name + i, description + i);
            taskManager.createTask(task);
            taskManager.getTaskById(task.getId());
        }
        Assertions.assertEquals(10, taskManager.getAllTasksInHistoryList().size(), "Количество задач в списке должно быть равно 10");
    }

    @Test
    void addTaskInHistoryList() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";

        Task task = taskManager.createTask(new Task(name, description));
        taskManager.getTaskById(task.getId());
        Task taskFromHistory = taskManager.getAllTasksInHistoryList().getFirst();

        Assertions.assertEquals(task, taskFromHistory, "Задача не добавилась в список историй");
    }

    @Test
    void testTaskWithSameIdAreEquals() {
        Task task1 = new Task(1, "Наименование 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task(1, "Наименование 2", "Описание 2", TaskStatus.DONE);
        Assertions.assertEquals(task1, task2, "Задачи должны быть равны по их id");
    }

    @Test
    void testEpicWithSameIdAreEquals() {
        Epic epic1 = new Epic(1, "Наименование 1", "Описание 1", TaskStatus.NEW);
        Epic epic2 = new Epic(1, "Наименование 2", "Описание 2", TaskStatus.DONE);
        Assertions.assertEquals(epic1, epic2, "Эпики должны быть равны по их id");
    }

    @Test
    void testSubtaskWithSameIdAreEquals() {
        Subtask subtask1 = new Subtask(1, "Наименование 1", "Описание 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(1, "Наименование 2", "Описание 2", TaskStatus.DONE, 0);
        Assertions.assertEquals(subtask1, subtask2, "Подзадачи должны быть равны по их id");
    }

    @Test
    void getTasks() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            taskManager.createTask(new Task(name + i, description + i));
        }
        Assertions.assertEquals(5, taskManager.getTasks().size(), "Количество задач в списке должно быть равным 5");
    }

    @Test
    void getSubtasks() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            taskManager.createEpic(new Epic(name + i, description + i));
        }
        Assertions.assertEquals(5, taskManager.getEpics().size(), "Количество подзадач в списке должно быть равным 5");
    }

    @Test
    void getEpics() {
        String name = "Наименование ";
        String description = "Описание ";
        Epic epic = new Epic(name + 1, description + 1);
        taskManager.createEpic(epic);
        Epic actualEpic = taskManager.getEpicById(epic.getId());
        for (int i = 2; i < 7; i++) {
            taskManager.createSubtask(new Subtask(name + i, actualEpic.getId(), description + i, TaskStatus.NEW));
        }
        Assertions.assertEquals(5, taskManager.getSubtasks().size(), "Количество эпиков в списке должно быть равным 5");
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

        Assertions.assertEquals(7, taskManager.getTasks().size(), "Количество задач в списке должно быть равным 7");
        Assertions.assertNotEquals(createdTask.getId(), createdTaskByFilledId.getId(), "Задачи с заданным id и сгенерированным id" +
                                                                                       " не должны конфликтовать внутри менеджера");
    }


    @Test
    void createTask() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";
        Task task = new Task(name, description);

        taskManager.createTask(task);
        Task actualTask = taskManager.getTaskById(task.getId());

        Assertions.assertNotNull(actualTask.getId(), "Задача не найдена");
        Assertions.assertEquals(TaskStatus.NEW, actualTask.getStatus(), "Неверно установлен статус задачи");
        Assertions.assertEquals(description, actualTask.getDescription(), "Неверно установлено описание задачи");
        Assertions.assertEquals(name, actualTask.getName(), "Неверно установлено наименование задачи");
    }

    @Test
    void createEpic() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";
        Epic epic = new Epic(name, description);

        taskManager.createEpic(epic);
        Epic actualEpic = taskManager.getEpicById(epic.getId());

        Assertions.assertNotNull(actualEpic.getId(), "Эпик не найден");
        Assertions.assertEquals(TaskStatus.NEW, actualEpic.getStatus(), "Неверно установлен статус эпика");
        Assertions.assertEquals(description, actualEpic.getDescription(), "Неверно установлено описание эпика");
        Assertions.assertEquals(name, actualEpic.getName(), "Неверно установлено наименование задачи");
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
        Task actualSubtask = taskManager.getSubtaskById(subtask.getId());

        Assertions.assertNotNull(actualSubtask.getId(), "Подзадача не найдена");
        Assertions.assertEquals(TaskStatus.NEW, actualSubtask.getStatus(), "Неверно установлен статус подзадачи");
        Assertions.assertEquals(subtaskName, actualSubtask.getName(), "Неверно установлено наименование подзадачи");
        Assertions.assertEquals(subtaskDescription, actualSubtask.getDescription(), "Неверно установлено описание подзадачи");
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
        Assertions.assertTrue(taskManager.getEpics().isEmpty(), "Эпик остался в списке после удаления");
        Assertions.assertTrue(taskManager.getSubtasks().isEmpty(), "При удалении эпика необходимо удалить его подзадачи");
    }

    @Test
    void ifSubtasksHaveDoneStatusThenEpicShouldHaveDoneStatus() {
        Epic epic = taskManager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.DONE));
        }
        Assertions.assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus(), "Эпик должен иметь статус 'выполненный'," +
                                                                                                    " т.к. его подзадачи 'выполнены'");
    }

    @Test
    void ifSubtasksHaveNewStatusThenEpicShouldHaveNewStatus() {
        Epic epic = taskManager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.NEW));
        }
        Assertions.assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic.getId()).getStatus(), "Эпик должен иметь статус 'новый'," +
                                                                                                   " т.к. его подзадачи 'новые'");
    }

    @Test
    void ifSubtasksHaveDifferentStatusesThenEpicShouldHaveStatusOfInProgress() {
        Epic epic = taskManager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.NEW));
        }
        for (int i = 0; i < 5; i++) {
            taskManager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.DONE));
        }
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), "Эпик должен иметь статус 'в процессе'," +
                                                                                                           " т.к. его подзадачи имеют разные статусы");
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
        Assertions.assertEquals(statusInHistoryBeforeUpdate, taskInHistoryAfterUpdate.getStatus(), "Статус задачи должен остаться прежним в истории");
        Assertions.assertEquals(descriptionInHistoryBeforeUpdate, taskInHistoryAfterUpdate.getDescription(), "Описание задачи должно остаться прежним");
        Assertions.assertEquals(nameInHistoryBeforeUpdate, taskInHistoryAfterUpdate.getName(), "Наименование задачи должно остаться прежним");
    }
}