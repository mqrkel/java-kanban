package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> tasksHistory = new ArrayList<>(10);

    @Override
    public void addToHistory(Task task) {
        if (tasksHistory.size() == 10) {
            tasksHistory.removeFirst();
        }
        tasksHistory.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(tasksHistory);
    }
}
