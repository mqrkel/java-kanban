package model;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Duration.ZERO, LocalDateTime.now());
    }

    public Epic(Integer id, String name, String description, TaskStatus status) {
        super(id, name, description, status, Duration.ZERO, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "%d,%s,%s,%s,%s,%s,%s".formatted(this.getId(), TaskType.EPIC, this.getName(),
                this.getStatus(), this.getDescription(), this.getDuration(), this.getStartTime());
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public List<Integer> getSubtaskIdsByEpic() {
        return List.copyOf(subtaskIds);
    }

    public void clearIdsSubtasksByEpic() {
        subtaskIds.clear();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }
}
