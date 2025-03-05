package http.handler;

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

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            sendJson(exchange, gson().toJson(epics), 200);
        } else if (path.matches("^/epics/\\d+$")) {
            try {
                String epicId = path.split("/")[2];
                Epic epic = taskManager.getEpicById(Integer.parseInt(epicId)).orElseThrow(() -> new NotFoundException("Epic not found"));
                sendJson(exchange, gson().toJson(epic), 200);
            } catch (NumberFormatException e) {
                sendError(exchange, "Invalid epic ID", 400);
            }
        } else if (path.matches("^/epics/\\d+/subtasks$")) {
            try {

                String epicId = path.split("/")[2];
                List<Subtask> subtasks = taskManager.getSubtasksByEpicId(Integer.parseInt(epicId));
                sendJson(exchange, gson().toJson(subtasks), 200);
            } catch (NumberFormatException e) {
                sendError(exchange, "Invalid epic ID", 400);
            }
        } else sendError(exchange, "Invalid request", 400);
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/epics/\\d+$")) {
            try {
                String string = path.split("/")[2];
                int epicId = Integer.parseInt(string);
                taskManager.deleteEpic(epicId);
                sendJson(exchange, "Epic deleted successfully", 200);
            } catch (NumberFormatException e) {
                sendError(exchange, "Invalid epic ID", 400);
            }
        } else sendError(exchange, "Invalid request", 400);
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        Epic epicJson = gson().fromJson(body, Epic.class);
        if (path.equals("/epics")) {
            taskManager.createEpic(epicJson);
            sendJson(exchange, "Epic created successfully", 201);
        } else {
            sendError(exchange, "Invalid endpoint", 404);
        }
    }
}