package model;


public class Epic extends Task {

    public Epic(String name, String description) {
        super(name, description);

    }

    public Epic(Integer id, String name, String description, TaskStatus status) {
        super(id, name, description, status);

    }

    @Override
    public String toString() {
        return "%d,%s,%s,%s,%s".formatted(this.getId(), TaskType.EPIC, this.getName(),
                this.getStatus(), this.getDescription());
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }
}
