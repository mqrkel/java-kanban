package model;


public class Epic extends Task {


    public Epic(String name, String description) {
        super(name, description);
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
