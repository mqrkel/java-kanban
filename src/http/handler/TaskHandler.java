package http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }


    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        switch (method) {
            case "GET":
                handleGetRequest(exchange, path);
                break;
            case "POST":
                handlePostRequest(exchange);
                break;
            case "DELETE":
                handleDeleteRequest(exchange, path);
                break;
            default:
                sendError(exchange, "Method not allowed");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/tasks/\\d+$")) {
            int taskID = Integer.parseInt(path.split("/")[2]);

            if (taskManager.getTaskById(taskID).isEmpty()) {
                sendText(exchange, "Task not found", 404);
                return;
            }
            taskManager.deleteTask(taskID);
            sendText(exchange, "Task deleted successfully", 200);
        } else {
            sendText(exchange, "Invalid request", 400);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/tasks/\\d+$")) {
            int taskId = Integer.parseInt(path.split("/")[2]);
            Task task = taskManager.getTaskById(taskId)
                    .orElseThrow(() -> new NotFoundException("Task not found"));
            sendText(exchange, gson.toJson(task), 200);
        } else if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks();
            sendText(exchange, gson.toJson(tasks), 200);
        } else sendNotFound(exchange, "Invalid endpoint");
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        boolean hasId = json != null && json.has("id") && !json.get("id").isJsonNull();
        int taskId = hasId ? json.get("id").getAsInt() : -1;
        Task task = gson.fromJson(body, Task.class);
        if (hasId) {
            if (taskManager.getTaskById(taskId).isEmpty()) {
                sendError(exchange, "Task with id " + taskId + " not found");
                return;
            }
            if (!taskManager.validateTask(task)) {
                System.out.println("Validation failed for task update!");
                sendHasInteractions(exchange, "Task time conflicts with existing task");
                return;
            }
            taskManager.updateTask(taskId, task);
            sendText(exchange, "Task updated successfully", 201);
        } else {
            if (!taskManager.validateTask(task)) {
                System.out.println("Validation failed for new task!");
                sendHasInteractions(exchange, "Task time conflicts with existing task");
                return;
            }
            taskManager.createTask(task);
            sendText(exchange, "Task created successfully", 201);
        }
    }
}