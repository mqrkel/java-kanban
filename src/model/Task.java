package model;



public class Task  {
    private  String name;
    private String description;
    private TaskStatus status;
    private static int count = 1;
    private int id;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.id = count++;
    }


    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.NEW;
    }


    @Override
    public String toString() {
                return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                '}'+"\n".repeat(2);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

}
