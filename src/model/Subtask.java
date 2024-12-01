package model;

import service.TaskManager;


public class Subtask extends Task {
    public Subtask(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Subtask{name=" + getName() + ", status=" + getStatus() + "}";
    }
}
