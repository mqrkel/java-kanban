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
        return "\n" + "Epic{" +
               "name='" + getName() + '\'' +
               ", description='" + getDescription() + '\'' +
               ", status=" + getStatus() +
               ", id=" + getId() +
               '}';
    }
}
