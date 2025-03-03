package service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.HttpTaskServer;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final String HISTORY_URL = BASE_URL + "/history";


    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        gson = server.getGson();
        server.start();
    }
    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldReturnEmptyHistoryIfNoTasksViewed() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldReturnHistoryWithViewedTasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        Task task = manager.createTask(new Task(
                "Task 1", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 10, 0)
        ));
        Subtask subtask = manager.createSubtask(new Subtask(
                "Subtask 1", "Description", epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));

        viewTask(task.getId(), TaskType.TASK);
        viewTask(epic.getId(), TaskType.EPIC);
        viewTask(subtask.getId(), TaskType.SUBTASK);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(3, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subtask.getId(), history.get(2).getId());
    }

    private void viewTask(int taskId, TaskType type) throws IOException, InterruptedException {
        String endpoint = switch (type) {
            case TASK -> "/tasks/";
            case EPIC -> "/epics/";
            case SUBTASK -> "/subtasks/";
        };
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint + taskId))
                .GET()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}