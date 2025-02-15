package service;

import exceptions.ManagerCreateFileException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void init() {
        try {
            tempFile = File.createTempFile("test", ".csv");
        } catch (IOException e) {
            throw new ManagerCreateFileException("Не удалось создать файл по указанному пути");
        }
        manager = new FileBackedTaskManager(Managers.getDefaultHistory(),tempFile);
    }

    @AfterEach
    void clean() {
        try {
            Files.delete(tempFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить временный файл: " + tempFile.getAbsolutePath());
        }
    }

    @Test
    void testSaveAndLoad() {
        manager.createTask(new Task("Task 1", "Description 1"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 2"));
        manager.createSubtask(new Subtask("Subtask", "Description 3", epic.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(manager.getTasks(), loadedManager.getTasks());
        assertEquals(manager.getEpics(), loadedManager.getEpics());
        assertEquals(manager.getSubtasks(), loadedManager.getSubtasks());
    }

    @Test
    void testEmptyTasks() {
        manager.deleteTasks();
        manager.deleteEpics();
        manager.deleteSubtasks();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void testSaveTaskToFile() throws IOException {
        manager.createTask(new Task("Task 1", "Description 1"));

        String inputString = Files.readString(tempFile.toPath());

        assertTrue(inputString.contains("Task 1"));
        assertTrue(inputString.contains("Description 1"));
        assertTrue(inputString.contains("NEW"));
        assertTrue(inputString.contains("1"));
        assertTrue(inputString.contains("TASK"));
    }
    @Test
    void testSaveEpicAndSubtaskToFile() throws IOException {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        manager.createSubtask(new Subtask("Subtask 1", "Description 2", epic.getId()));

        String inputString = Files.readString(tempFile.toPath());
        System.out.println(inputString);

        assertTrue(inputString.contains("Subtask 1"));
        assertTrue(inputString.contains("Epic 1"));
        assertTrue(inputString.contains("Description 2"));
        assertTrue(inputString.contains("Description 1"));
        assertTrue(inputString.contains("NEW"));
        assertTrue(inputString.contains("1"));
        assertTrue(inputString.contains("2"));
        assertTrue(inputString.contains("EPIC"));
        assertTrue(inputString.contains("SUBTASK"));
    }

    @Test
    void testManagerInitialization() {
        Assertions.assertNotNull(manager);
    }

    @Test
    void addTasksOfDifferentTypesAndCanFindThemById() {
        Task task = manager.createTask(new Task("Наименование задачи", "Описание задачи"));
        Epic epic = manager.createEpic(new Epic("Наименование эпика", "Описание эпика"));
        Subtask subtask = manager.createSubtask(new Subtask("Наименование сабтаска", epic.getId(), "Описание сабтаска", TaskStatus.NEW));
        Task taskById = manager.getTaskById(task.getId());
        Epic epicById = manager.getEpicById(epic.getId());
        Subtask subtaskById = manager.getSubtaskById(subtask.getId());
        assertEquals(taskById, task, "Задачи не совпадают");
        assertEquals(epicById, epic, "Эпики не совпадают");
        assertEquals(subtaskById, subtask, "Подзадачи не совпадают");
    }

    @Test
    void addTasksToHistoryAndAddingTaskAgainShouldRewriteTaskToTheEndOfTheList() {
        String name = "Наименование ";
        String description = "Описание ";
        List<Task> evenNumbers = new ArrayList<>();

        for (int i = 1; i < 16; i++) {
            Task task = new Task(name + i, description + i);
            manager.createTask(task);
            Task taskById = manager.getTaskById(task.getId());
            if (i % 2 == 0) {
                evenNumbers.add(taskById);
            }
        }
        for (Task evenNumber : evenNumbers) {
            manager.getTaskById(evenNumber.getId());
        }
        assertEquals(15, manager.getAllTasksInHistoryList().size(), "Количество задач в списке должно быть равно 15");

        Task task = manager.getTaskById(14);
        List<Task> history = manager.getAllTasksInHistoryList();
        assertEquals(task, history.getLast(), "Задача с ID 14 должна быть в конце списка.");
    }

    @Test
    void addTaskInHistoryList() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";

        Task task = manager.createTask(new Task(name, description));
        manager.getTaskById(task.getId());
        Task taskFromHistory = manager.getAllTasksInHistoryList().getFirst();

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
            manager.createTask(new Task(name + i, description + i));
        }
        assertEquals(5, manager.getTasks().size(), "Количество задач в списке должно быть равным 5");
    }

    @Test
    void getSubtasks() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            manager.createEpic(new Epic(name + i, description + i));
        }
        assertEquals(5, manager.getEpics().size(), "Количество подзадач в списке должно быть равным 5");
    }

    @Test
    void getEpics() {
        String name = "Наименование ";
        String description = "Описание ";
        Epic epic = new Epic(name + 1, description + 1);
        manager.createEpic(epic);
        Epic actualEpic = manager.getEpicById(epic.getId());
        for (int i = 2; i < 7; i++) {
            manager.createSubtask(new Subtask(name + i, actualEpic.getId(), description + i, TaskStatus.NEW));
        }
        assertEquals(5, manager.getSubtasks().size(), "Количество эпиков в списке должно быть равным 5");
    }

    @Test
    void idsByTasksShouldBeUnique() {
        String name = "Наименование задачи ";
        String description = "Описание задачи ";

        Task task = new Task(name + "1", description + "1");
        Task createdTask = manager.createTask(task);
        for (int i = 2; i < 7; i++) {
            manager.createTask(new Task(name + i, description + i));
        }
        Task createdTaskByFilledId = manager.createTask(new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()));

        assertEquals(7, manager.getTasks().size(), "Количество задач в списке должно быть равным 7");
        Assertions.assertNotEquals(createdTask.getId(), createdTaskByFilledId.getId(), "Задачи с заданным id и сгенерированным id" +
                                                                                       " не должны конфликтовать внутри менеджера");
    }


    @Test
    void createTask() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";
        Task task = new Task(name, description);

        manager.createTask(task);
        Task actualTask = manager.getTaskById(task.getId());

        Assertions.assertNotNull(actualTask.getId(), "Задача не найдена");
        assertEquals(TaskStatus.NEW, actualTask.getStatus(), "Неверно установлен статус задачи");
        assertEquals(description, actualTask.getDescription(), "Неверно установлено описание задачи");
        assertEquals(name, actualTask.getName(), "Неверно установлено наименование задачи");
    }

    @Test
    void createEpic() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";
        Epic epic = new Epic(name, description);

        manager.createEpic(epic);
        Epic actualEpic = manager.getEpicById(epic.getId());

        Assertions.assertNotNull(actualEpic.getId(), "Эпик не найден");
        assertEquals(TaskStatus.NEW, actualEpic.getStatus(), "Неверно установлен статус эпика");
        assertEquals(description, actualEpic.getDescription(), "Неверно установлено описание эпика");
        assertEquals(name, actualEpic.getName(), "Неверно установлено наименование задачи");
    }

    @Test
    void createSubtask() {
        String subtaskName = "Наименование подзадачи 1";
        String subtaskDescription = "Описание подзадачи 1";
        String epicName = "Название эпика 1";
        String epicDescription = "Описание эпика 1";

        Epic epic = new Epic(epicName, epicDescription);
        manager.createEpic(epic);

        Subtask subtask = new Subtask(subtaskName, epic.getId(), subtaskDescription, TaskStatus.NEW);

        manager.createSubtask(subtask);
        Task actualSubtask = manager.getSubtaskById(subtask.getId());

        Assertions.assertNotNull(actualSubtask.getId(), "Подзадача не найдена");
        assertEquals(TaskStatus.NEW, actualSubtask.getStatus(), "Неверно установлен статус подзадачи");
        assertEquals(subtaskName, actualSubtask.getName(), "Неверно установлено наименование подзадачи");
        assertEquals(subtaskDescription, actualSubtask.getDescription(), "Неверно установлено описание подзадачи");
    }

    @Test
    void removingEpicShouldRemoveAllSubtasks() {
        String name = "Наименование подзадачи ";
        String description = "Описание подзадачи ";
        Epic epic = manager.createEpic(new Epic("Наименование эпика", "Описание эпика"));
        for (int i = 0; i < 5; i++) {
            manager.createSubtask(new Subtask(name + i, epic.getId(), description + i, TaskStatus.NEW));
        }
        manager.deleteEpic(epic.getId());
        assertTrue(manager.getEpics().isEmpty(), "Эпик остался в списке после удаления");
        assertTrue(manager.getSubtasks().isEmpty(), "При удалении эпика необходимо удалить его подзадачи");
    }

    @Test
    void ifSubtasksHaveDoneStatusThenEpicShouldHaveDoneStatus() {
        Epic epic = manager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            manager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.DONE));
        }
        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus(), "Эпик должен иметь статус 'выполненный'," +
                                                                                     " т.к. его подзадачи 'выполнены'");
    }

    @Test
    void ifSubtasksHaveNewStatusThenEpicShouldHaveNewStatus() {
        Epic epic = manager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            manager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.NEW));
        }
        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus(), "Эпик должен иметь статус 'новый'," +
                                                                                    " т.к. его подзадачи 'новые'");
    }

    @Test
    void ifSubtasksHaveDifferentStatusesThenEpicShouldHaveStatusOfInProgress() {
        Epic epic = manager.createEpic(new Epic(1, "Наименование", "Описание", TaskStatus.NEW));
        for (int i = 0; i < 5; i++) {
            manager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.NEW));
        }
        for (int i = 0; i < 5; i++) {
            manager.createSubtask(new Subtask("Наименование", epic.getId(), "Описание", TaskStatus.DONE));
        }
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(), "Эпик должен иметь статус 'в процессе'," +
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

        Task createdTask = manager.createTask(task);
        manager.getTaskById(task.getId());
        Task taskInHistory = manager.getAllTasksInHistoryList().getFirst();

        TaskStatus statusInHistoryBeforeUpdate = taskInHistory.getStatus();
        String descriptionInHistoryBeforeUpdate = taskInHistory.getDescription();
        String nameInHistoryBeforeUpdate = taskInHistory.getName();

        manager.updateTask(createdTask.getId(), modifiedTask);

        Task taskInHistoryAfterUpdate = manager.getAllTasksInHistoryList().getFirst();
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
            manager.createEpic(epic);
            manager.getEpicById((epic.getId()));
        }
        for (int i = 0; i < 5; i++) {
            Task task = new Task(name + i, description + i);
            manager.createTask(task);
            manager.getTaskById((task.getId()));
        }
        manager.deleteTasks();
        assertEquals(5, manager.getAllTasksInHistoryList().size(), "При удалении задачи, список в истории должен быть уменьшен на 5");
    }

    @Test
    void WhenRemoveEpicFromTasksListAlsoEpicShouldBeDeletedFromHistoryList() {
        String name = "Наименование ";
        String description = "Описание ";
        for (int i = 0; i < 5; i++) {
            Epic epic = new Epic(name + i, description + i);
            manager.createEpic(epic);
            manager.getEpicById((epic.getId()));
        }
        manager.deleteEpic(1);
        assertEquals(4, manager.getAllTasksInHistoryList().size(), "При удалении задачи, список в истории должен быть уменьшен на 1");
    }

    @Test
    void WhenRemoveSubtaskFromTasksListAlsoSubtaskShouldBeDeletedFromHistoryList() {
        String name = "Наименование ";
        String description = "Описание ";
        Epic epic = manager.createEpic(new Epic(name + 1, description + 1));
        for (int i = 0; i < 5; i++) {
            Subtask subtask = new Subtask(name + i, description + i, epic.getId());
            manager.createSubtask(subtask);
            manager.getSubtaskById((subtask.getId()));
        }
        manager.deleteSubtask(2);
        assertEquals(4, manager.getAllTasksInHistoryList().size(), "При удалении задачи, список в истории должен быть уменьшен на 1");
    }
}