package service;

import model.Node;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> historyMap = new HashMap<>();
    private final List<Task> tasksHistory = new ArrayList<>();
    private Node head;
    private Node tail;


    @Override
    public void addToHistory(Task task) {
        if (historyMap.containsKey(task.getId())) {
            removeNode(historyMap.get(task.getId()));
        }
        Node node = new Node(task);
        historyMap.put(task.getId(), node);
        linkLast(node);
    }

    void linkLast(Node newNode) {
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else head = node.next;


        if (node.next != null) {
            node.next.prev = node.prev;
        } else tail = node.prev;
    }

    @Override
    public List<Task> getHistory() {
        tasksHistory.clear();
        Node current = head;
        while (current != null) {
            tasksHistory.add(current.task);
            current = current.next;
        }
        return tasksHistory;
    }

    @Override
    public void removeFromTaskHistory(Integer id) {
        tasksHistory.removeIf(task -> task.getId() == id);
        Node node = historyMap.get(id);
        removeNode(node);
    }
}
