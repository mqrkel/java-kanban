package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/history")) {
            handleGetRequest(exchange);
        } else {
            sendNotFound(exchange, "Invalid endpoint");
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        List<Task> tasksInHistoryList = taskManager.getAllTasksInHistoryList();
        sendText(exchange, gson.toJson(tasksInHistoryList), 200);
    }
}