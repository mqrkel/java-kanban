package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    List<Subtask> subtasks;


    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void checkStatus() {
        ArrayList<Subtask> list = new ArrayList<>(subtasks);
        if (!list.isEmpty()) {
            int result = 0;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() == TaskStatus.DONE) {
                    result++;
                }
                if (list.size() == result) this.setStatus(TaskStatus.DONE);
                else if (result != 0) {
                    this.setStatus(TaskStatus.IN_PROGRESS);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", id=" + getId() +
                ", subtasks=" + subtasks +
                '}' + "\n".repeat(2);
    }
}
