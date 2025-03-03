package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Epic;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.*;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
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
                handlePostRequest(exchange, path);
                break;
            case "DELETE":
                handleDeleteRequest(exchange, path);
                break;
            default:
                sendError(exchange, "Method not allowed");
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            sendText(exchange, gson.toJson(epics), 200);
        } else if (path.matches("^/epics/\\d+$")) {
            String epicId = path.split("/")[2];
            Epic epic = taskManager.getEpicById(Integer.parseInt(epicId)).orElseThrow(() -> new NotFoundException("Epic not found"));
            sendText(exchange, gson.toJson(epic), 200);
        } else if (path.matches("^/epics/\\d+/subtasks$")) {
            String epicId = path.split("/")[2];
            List<Subtask> subtasks = taskManager.getSubtasksByEpicId(Integer.parseInt(epicId));
            sendText(exchange, gson.toJson(subtasks), 200);
        } else sendNotFound(exchange, "Invalid endpoint");
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/epics/\\d+$")) {
            String string = path.split("/")[2];
            int epicId = Integer.parseInt(string);
            taskManager.deleteEpic(epicId);
            sendText(exchange, "Epic deleted successfully", 200);
        } else sendNotFound(exchange, "Invalid endpoint");
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        Epic epicJson = gson.fromJson(body, Epic.class);
        if (path.equals("/epics")) {
            taskManager.createEpic(epicJson);
            sendText(exchange, "Epic created successfully", 201);
        } else {
            sendNotFound(exchange, "Invalid endpoint");
        }
    }
}