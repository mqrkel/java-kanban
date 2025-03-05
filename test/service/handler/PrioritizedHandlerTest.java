package service.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import http.HttpTaskServer;
import http.handler.adapter.DurationAdapter;
import http.handler.adapter.LocalDateTimeAdapter;
import model.Epic;
import model.Subtask;
import model.Task;
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

class PrioritizedHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT + "/prioritized";

    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldReturnEmptyListIfNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(prioritizedTasks.isEmpty());
    }

    @Test
    void shouldReturnTasksSortedByStartTime() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task(
                "Task 1", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 10, 0)
        ));
        Task task2 = manager.createTask(new Task(
                "Task 2", "Description",
                Duration.ofMinutes(45),
                LocalDateTime.of(2025, 3, 1, 9, 15)
        ));
        Task task3 = manager.createTask(new Task(
                "Task 3", "Description",
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 3, 1, 11, 0)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());

        assertEquals(3, prioritizedTasks.size());
        assertEquals(task2.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task1.getId(), prioritizedTasks.get(1).getId());
        assertEquals(task3.getId(), prioritizedTasks.get(2).getId());
    }
    @Test
    void shouldIncludeSubtasksInPrioritizedList() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        Task task = manager.createTask(new Task(
                "Task 1", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 10, 0)
        ));
        Subtask subtask = manager.createSubtask(new Subtask(
                "Subtask 1", "Description", epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 9, 30)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());

        assertEquals(2, prioritizedTasks.size());
        assertEquals(subtask.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task.getId(), prioritizedTasks.get(1).getId());
    }
}