package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(String name, int epicId, String description, TaskStatus status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(Integer id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        setStatus(TaskStatus.NEW);
        this.epicId = epicId;
    }

    public Subtask(Integer idTask, String nameTask, String descriptionTask, TaskStatus statusTask, int epicId, Duration duration, LocalDateTime startTime) {
        super(idTask, nameTask, descriptionTask, statusTask, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.epicId = epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "%d,%s,%s,%s,%s,%d,%s,%s".formatted(this.getId(), TaskType.SUBTASK, this.getName(),
                this.getStatus(), this.getDescription(), this.getEpicId(), this.getDuration(), this.getStartTime()
        );
    }

}
