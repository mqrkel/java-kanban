package service;

import exceptions.ManagerCreateFileException;
import exceptions.ManagerReadFileException;
import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static service.Managers.getDefaultHistory;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    public static void main(String[] args) {
        File file = Path.of("resources", "logs.csv").toFile();
        FileBackedTaskManager taskManager = new FileBackedTaskManager(getDefaultHistory(), file);

        taskManager.createTask(new Task("Кот", "Покорми кота"));
        taskManager.createTask(new Task("Магазин", "Сходи в магазин"));
        Epic epic1 = taskManager.createEpic(new Epic("Путешествие", "Приготовиться к путешествию"));
        taskManager.createEpic(new Epic("Новый год", "Подготовься к новому году"));
        taskManager.createSubtask(new Subtask("Документы", "Проверить документы", epic1.getId()));
        taskManager.createSubtask(new Subtask("Вещи", "Собрать вещи", epic1.getId()));
        taskManager.createSubtask(new Subtask("Кот", "Отдать кота", epic1.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        System.out.println(loadedManager.getTasks());
        System.out.println(loadedManager.getEpics());
        System.out.println(loadedManager.getSubtasks());
        loadedManager.deleteTasks();
    }


    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
        checkFileExist();
    }

    private void checkFileExist() {
        if (!file.exists()) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new ManagerCreateFileException("Не удалось создать директорию/файл по указанному пути");
            }
        }
    }

    public static Task parseFromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Неправильный формат: " + value);
        }
        Integer idTask = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String nameTask = parts[2];
        TaskStatus statusTask = TaskStatus.valueOf(parts[3]);
        String descriptionTask = parts[4];

        switch (type) {
            case TASK -> {
                return new Task(idTask, nameTask, descriptionTask, statusTask);
            }
            case EPIC -> {
                return new Epic(idTask, nameTask, descriptionTask, statusTask);
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(idTask, nameTask, descriptionTask, statusTask, epicId);
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                writer.write(task.toString());
                writer.newLine();
            }
            for (Epic epic : getEpics()) {
                writer.write(epic.toString());
                writer.newLine();
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(subtask.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить логи в файл");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                Task task = parseFromString(line);
                if (task instanceof Subtask) {
                    manager.createSubtask((Subtask) task);
                } else if (task instanceof Epic) {
                    manager.createEpic((Epic) task);
                } else manager.createTask(task);
            }
        } catch (IOException e) {
            throw new ManagerReadFileException("Не удалось найти файл по указанному пути");
        }
        return manager;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public Task updateTask(int taskId, Task modifiedTask) {
        Task updatedTask = super.updateTask(taskId, modifiedTask);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(int epicId, Epic modifiedEpic) {
        Epic updatedEpic = super.updateEpic(epicId, modifiedEpic);
        save();
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(int subtaskId, Subtask modifiedSubtask) {
        Subtask updatedSubtask = super.updateSubtask(subtaskId, modifiedSubtask);
        save();
        return updatedSubtask;
    }

    @Override
    public Task deleteTask(int taskId) {
        Task deletedTask = super.deleteTask(taskId);
        save();
        return deletedTask;
    }

    @Override
    public Epic deleteEpic(int epicId) {
        Epic deletedEpic = super.deleteEpic(epicId);
        save();
        return deletedEpic;
    }

    @Override
    public Subtask deleteSubtask(int subtaskId) {
        Subtask deletedSubtask = super.deleteSubtask(subtaskId);
        save();
        return deletedSubtask;
    }
}