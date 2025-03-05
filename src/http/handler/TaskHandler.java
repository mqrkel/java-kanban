package http.handler;

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

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("^/tasks/\\d+$")) {
            int taskID = Integer.parseInt(path.split("/")[2]);

            if (taskManager.getTaskById(taskID).isEmpty()) {
                sendJson(exchange, "Task not found", 404);
                return;
            }
            taskManager.deleteTask(taskID);
            sendJson(exchange, "Task deleted successfully", 200);
        } else {
            sendJson(exchange, "Invalid request", 400);
        }
    }

    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.matches("^/tasks/\\d+$")) {
                int taskId = Integer.parseInt(path.split("/")[2]);
                Task task = taskManager.getTaskById(taskId)
                        .orElseThrow(() -> new NotFoundException("Task not found"));
                sendJson(exchange, gson().toJson(task), 200);
            } else if (path.equals("/tasks")) {
                List<Task> tasks = taskManager.getTasks();
                sendJson(exchange, gson().toJson(tasks), 200);
            } else {
                throw new NotFoundException("Invalid request");
            }
        } catch (NotFoundException e) {
            sendError(exchange, e.getMessage(), 404);
        } catch (Exception e) {
            sendError(exchange, "Iternal server error", 500);
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = gson().fromJson(body, JsonObject.class);

        boolean hasId = json != null && json.has("id") && !json.get("id").isJsonNull();
        int taskId = hasId ? json.get("id").getAsInt() : -1;
        Task task = gson().fromJson(body, Task.class);
        if (hasId) {
            if (taskManager.getTaskById(taskId).isEmpty()) {
                sendError(exchange, "Task with id " + taskId + " not found", 404);
                return;
            }
            if (!taskManager.validateTask(task)) {
                System.out.println("Validation failed for task update!");
                sendError(exchange, "Task time conflicts with existing task", 406);
                return;
            }
            taskManager.updateTask(taskId, task);
            sendJson(exchange, "Task updated successfully", 201);
        } else {
            if (!taskManager.validateTask(task)) {
                System.out.println("Validation failed for new task!");
                sendError(exchange, "Task time conflicts with existing task", 406);
                return;
            }
            taskManager.createTask(task);
            sendJson(exchange, "Task created successfully", 201);
        }
    }
}