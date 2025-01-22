package model;

public class Node {
    public Task task;
    public Node next;
    public Node prev;


    public Node(Task task) {
        this.task = task;
    }

}
