package service;

import exceptions.ManagerCreateFileException;
import exceptions.ManagerReadFileException;
import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

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
        if (parts.length < 7) {
            throw new IllegalArgumentException("Неправильный формат: " + value);
        }
        Integer idTask = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String nameTask = parts[2];
        TaskStatus statusTask = TaskStatus.valueOf(parts[3]);
        String descriptionTask = parts[4];
        Duration duration = parts[6].equals("null") ? Duration.ZERO : Duration.ofMinutes(Long.parseLong(parts[6]));
        LocalDateTime startTime = parts[7].equals("null") ? null : LocalDateTime.parse(parts[7]);

        switch (type) {
            case TASK -> {
                return new Task(idTask, nameTask, descriptionTask, statusTask, duration, startTime);
            }
            case EPIC -> {
                return new Epic(idTask, nameTask, descriptionTask, statusTask, duration, startTime);
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(idTask, nameTask, descriptionTask, statusTask, epicId, duration, startTime);
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private void saveToFileCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epicId,duration,startTime" + System.lineSeparator());
            getTasks().stream().map(this::taskToString).forEach(task -> writeLine(writer, task));
            getEpics().stream().map(this::taskToString).forEach(task -> writeLine(writer, task));
            getSubtasks().stream().map(this::taskToString).forEach(task -> writeLine(writer, task));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл");
        }
    }

    private void writeLine(BufferedWriter writer, String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи строки в файл");
        }
    }

    private String taskToString(Task task) {
        return String.format("%d,%s,%s,%s,%s,%s,%d,%s", task.getId(), task.getTaskType(), task.getName(), task.getStatus(), task.getDescription(), task.getTaskType().equals(TaskType.SUBTASK) ? ((Subtask) task).getEpicId() : "", task.getDuration() != null ? task.getDuration().toMinutes() : 0, task.getStartTime() != null ? task.getStartTime() : "null");
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        int maxId = 0;
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                Task task = parseFromString(line);
                maxId = Math.max(maxId, task.getId());
                if (TaskType.TASK.equals(task.getTaskType())) {
                    manager.createTask(task);
                } else if (TaskType.EPIC.equals(task.getTaskType())) {
                    manager.createEpic((Epic) task);
                } else manager.createSubtask((Subtask) task);
            }
        } catch (IOException e) {
            throw new ManagerReadFileException("Не удалось найти файл по указанному пути");
        }
        manager.generatorId = maxId;
        return manager;
    }


    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        saveToFileCsv();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        saveToFileCsv();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        saveToFileCsv();
        return createdSubtask;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        saveToFileCsv();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        saveToFileCsv();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        saveToFileCsv();
    }

    @Override
    public Task updateTask(int taskId, Task modifiedTask) {
        Task updatedTask = super.updateTask(taskId, modifiedTask);
        saveToFileCsv();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(int epicId, Epic modifiedEpic) {
        Epic updatedEpic = super.updateEpic(epicId, modifiedEpic);
        saveToFileCsv();
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(int subtaskId, Subtask modifiedSubtask) {
        Subtask updatedSubtask = super.updateSubtask(subtaskId, modifiedSubtask);
        saveToFileCsv();
        return updatedSubtask;
    }

    @Override
    public Task deleteTask(int taskId) {
        Task deletedTask = super.deleteTask(taskId);
        saveToFileCsv();
        return deletedTask;
    }

    @Override
    public Epic deleteEpic(int epicId) {
        Epic deletedEpic = super.deleteEpic(epicId);
        saveToFileCsv();
        return deletedEpic;
    }

    @Override
    public Subtask deleteSubtask(int subtaskId) {
        Subtask deletedSubtask = super.deleteSubtask(subtaskId);
        saveToFileCsv();
        return deletedSubtask;
    }
}