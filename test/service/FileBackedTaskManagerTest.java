package service;

import exceptions.ManagerCreateFileException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    @BeforeEach
    public void init() {
        try {
            tempFile = File.createTempFile("test", ".csv");
        } catch (IOException e) {
            throw new ManagerCreateFileException("Не удалось создать файл по указанному пути");
        }
        taskManager = createTaskManager();
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), tempFile);
        return taskManager;
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
    void testSaveToFileCsvAndLoad() {
        taskManager.createTask(new Task("Task 1", "Description 1"));
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 2"));
        taskManager.createSubtask(new Subtask("Subtask", "Description 3", epic.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(taskManager.getTasks(), loadedManager.getTasks());
        assertEquals(taskManager.getEpics(), loadedManager.getEpics());
        assertEquals(taskManager.getSubtasks(), loadedManager.getSubtasks());
    }

    @Test
    void testEpicAndSubtaskTimeSavingAndLoading() {
        Epic epic = taskManager.createEpic(new Epic("Epic with time", "Epic description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask with time", "Subtask description", epic.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 2, 21, 14, 0)));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Optional<Epic> loadedEpic = loadedManager.getEpicById(epic.getId());
        Optional<Subtask> loadedSubtask = loadedManager.getSubtaskById(subtask.getId());

        assertTrue(loadedEpic.isPresent(), "Эпик не загружен");
        assertTrue(loadedSubtask.isPresent(), "Подзадача не загружена");

        assertEquals(subtask.getStartTime(), loadedSubtask.get().getStartTime(), "Время начала подзадачи не совпадает");
        assertEquals(subtask.getDuration(), loadedSubtask.get().getDuration(), "Продолжительность подзадачи не совпадает");
    }

    @Test
    void testSaveAndLoadEpicWithSubtasksAndTime() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Epic Description"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Subtask Description", epic.getId(), Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 22, 10, 0)));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Subtask Description", epic.getId(), Duration.ofMinutes(60), LocalDateTime.of(2025, 2, 22, 11, 0)));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Optional<Epic> loadedEpic = loadedManager.getEpicById(epic.getId());
        Optional<Subtask> loadedSubtask1 = loadedManager.getSubtaskById(subtask1.getId());
        Optional<Subtask> loadedSubtask2 = loadedManager.getSubtaskById(subtask2.getId());

        assertTrue(loadedEpic.isPresent(), "Эпик не загружен");
        assertTrue(loadedSubtask1.isPresent(), "Первая подзадача не загружена");
        assertTrue(loadedSubtask2.isPresent(), "Вторая подзадача не загружена");

        assertEquals(subtask1.getStartTime(), loadedSubtask1.get().getStartTime(), "Время первой подзадачи не совпадает");
        assertEquals(subtask2.getStartTime(), loadedSubtask2.get().getStartTime(), "Время второй подзадачи не совпадает");
        assertEquals(subtask1.getDuration(), loadedSubtask1.get().getDuration(), "Длительность первой подзадачи не совпадает");
        assertEquals(subtask2.getDuration(), loadedSubtask2.get().getDuration(), "Длительность второй подзадачи не совпадает");
    }


    @Test
    void testSaveToFileCsvAndLoadWithTime() {
        taskManager.createTask(new Task("Task 1", "Description 1", Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 12, 30)));
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 2"));
        taskManager.createSubtask(new Subtask("Subtask", "Description 3", epic.getId(), Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 13, 30)));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(taskManager.getTasks(), loadedManager.getTasks());
        assertEquals(taskManager.getEpics(), loadedManager.getEpics());
        assertEquals(taskManager.getSubtasks(), loadedManager.getSubtasks());
    }

    @Test
    void testEmptyTasks() {
        taskManager.deleteTasks();
        taskManager.deleteEpics();
        taskManager.deleteSubtasks();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void testSaveToFileCsvTaskToFile() throws IOException {
        taskManager.createTask(new Task("Task 1", "Description 1"));

        String inputString = Files.readString(tempFile.toPath());

        assertTrue(inputString.contains("Task 1"));
        assertTrue(inputString.contains("Description 1"));
        assertTrue(inputString.contains("NEW"));
        assertTrue(inputString.contains("1"));
        assertTrue(inputString.contains("TASK"));
    }

    @Test
    void testSaveToFileCsvTaskToFileWithTime() throws IOException {
        taskManager.createTask(new Task("Task 1", "Description 1", Duration.ofMinutes(30), LocalDateTime.of(2025, 2, 20, 12, 0)));

        String inputString = Files.readString(tempFile.toPath());
        System.out.println("inputString = " + inputString);
        assertTrue(inputString.contains("Task 1"));
        assertTrue(inputString.contains("Description 1"));
        assertTrue(inputString.contains("NEW"));
        assertTrue(inputString.contains("1"));
        assertTrue(inputString.contains("TASK"));
        assertTrue(inputString.contains("2025-02-20T12:00"));
        assertTrue(inputString.contains("30"));
    }

    @Test
    void testSaveToFileCsvEpicAndSubtaskToFile() throws IOException {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        taskManager.createSubtask(new Subtask("Subtask 1", "Description 2", epic.getId()));

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

    @Override
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

    @Override
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

    @Override
    @Test
    void addTaskInHistoryList() {
        String name = "Наименование задачи 1";
        String description = "Описание задачи 1";

        Task task = taskManager.createTask(new Task(name, description));
        taskManager.getTaskById(task.getId());
        Task taskFromHistory = taskManager.getAllTasksInHistoryList().getFirst();

        assertEquals(task, taskFromHistory, "Задача не добавилась в список историй");
    }
}