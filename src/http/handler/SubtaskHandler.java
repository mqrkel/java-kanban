package http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
        if (path.matches("^/subtasks/\\d+$")) {
            int subtaskID = Integer.parseInt(path.split("/")[2]);

            if (taskManager.getSubtaskById(subtaskID).isEmpty()) {
                sendText(exchange, "Subtask not found", 404);
                return;
            }
            taskManager.deleteSubtask(subtaskID);
            sendText(exchange, "Subtask deleted successfully", 200);
        } else {
            sendText(exchange, "Invalid request", 400);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/subtasks/\\d+$")) {
            int subtaskId = Integer.parseInt(path.split("/")[2]);
            Subtask subtask = taskManager.getSubtaskById(subtaskId)
                    .orElseThrow(() -> new NotFoundException("Subtask not found"));
            sendText(exchange, gson.toJson(subtask), 200);
        } else if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            sendText(exchange, gson.toJson(subtasks), 200);
        } else sendNotFound(exchange, "Invalid endpoint");
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        boolean hasId = json != null && json.has("id") && !json.get("id").isJsonNull();
        int subtaskId = hasId ? json.get("id").getAsInt() : -1;
        Subtask subtask = gson.fromJson(body, Subtask.class);
        if (hasId) {
            if (taskManager.getSubtaskById(subtaskId).isEmpty()) {
                sendError(exchange, "Subtask with id " + subtaskId + " not found");
                return;
            }
            if (!taskManager.validateTask(subtask)) {
                System.out.println("Validation failed for subtask update!");
                sendHasInteractions(exchange, "Subtask time conflicts with existing subtask");
                return;
            }
            taskManager.updateSubtask(subtaskId, subtask);
            sendText(exchange, "Subtask updated successfully", 201);
        } else {
            if (!taskManager.validateTask(subtask)) {
                System.out.println("Validation failed for new subtask!");
                sendHasInteractions(exchange, "Subtask time conflicts with existing subtask");
                return;
            }
            taskManager.createSubtask(subtask);
            sendText(exchange, "Subtask created successfully", 201);
        }
    }
}