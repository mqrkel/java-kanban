package model;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Epic extends Task {
    Set<Integer> subtaskIds = new HashSet<>();

    public Epic(String name, String description) {
        super(name, description, Duration.ZERO, null);
    }

    public Epic(Integer id, String name, String description, TaskStatus status) {
        super(id, name, description, status, Duration.ZERO, null);
    }

    public Epic(Integer idTask, String nameTask, String descriptionTask, TaskStatus statusTask, Duration duration, LocalDateTime startTime) {
        super(idTask, nameTask, descriptionTask, statusTask, duration, startTime);
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
