package service.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exceptions.NotFoundException;
import http.HttpTaskServer;
import http.handler.adapter.DurationAdapter;
import http.handler.adapter.LocalDateTimeAdapter;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubtaskHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT + "/subtasks";

    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;
    private Epic epic;

    @BeforeEach
    void setUp() throws IOException {
        startServer();
        epic = manager.createEpic(new Epic("Epic 1", "Epic description"));
    }

    private void startServer() throws IOException {
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
    void shouldCreateSubtaskSuccessfully() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(
                "Subtask 1",
                "Description",
                epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        );

        String jsonSubtask = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getSubtasksByEpicId(epic.getId());
        assertEquals(1, subtasks.size());
        assertEquals("Subtask 1", subtasks.get(0).getName());
        assertEquals(Duration.ofMinutes(30), subtasks.get(0).getDuration());
        assertEquals(LocalDateTime.of(2025, 3, 1, 12, 0), subtasks.get(0).getStartTime());
    }

    @Test
    void shouldUpdateSubtaskSuccessfully() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(
                "Subtask 1",
                "Description",
                epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        );

        String jsonTask = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasks = gson.fromJson(getResponse.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        int subtaskId = subtasks.get(subtasks.size() - 1).getId();

        Subtask taskForUpdate = new Subtask(
                subtaskId,
                "Updated Subtask 1",
                "Updated Description",
                epic.getId(),
                Duration.ofMinutes(45),
                LocalDateTime.of(2025, 3, 2, 12, 0)
        );

        String jsonUpdate = gson.toJson(taskForUpdate);

        HttpRequest requestUpdate = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonUpdate))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseUpdate.statusCode());
        HttpRequest getUpdatedRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> getUpdatedResponse = client.send(getUpdatedRequest, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasksAfterUpdate = gson.fromJson(getUpdatedResponse.body(), new TypeToken<List<Subtask>>() {
        }.getType());

        Subtask updateSubtask = subtasksAfterUpdate.stream()
                .filter(t -> t.getId() == subtaskId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Updated subtask not found"));

        assertEquals(subtaskId, updateSubtask.getId());
        assertEquals("Updated Subtask 1", updateSubtask.getName());
        assertEquals("Updated Description", updateSubtask.getDescription());
        assertEquals(Duration.ofMinutes(45), updateSubtask.getDuration());
        assertEquals(LocalDateTime.of(2025, 3, 2, 12, 0), updateSubtask.getStartTime());
    }

    @Test
    void shouldReturnAllSubtasks() throws IOException, InterruptedException {
        Subtask subtask1 = manager.createSubtask(new Subtask(
                "Subtask 1", "Description", epic.getId(),
                Duration.ofMinutes(45),
                LocalDateTime.of(2025, 3, 1, 14, 0)
        ));
        Subtask subtask2 = manager.createSubtask(new Subtask(
                "Subtask 2", "Description", epic.getId(),
                Duration.ofMinutes(60),
                LocalDateTime.of(2025, 3, 1, 15, 0)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Subtask>>() {
        }.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), listType);

        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
    }

    @Test
    void shouldReturnSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask(
                "Subtask 1", "Description", epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), retrievedSubtask.getId());
        assertEquals("Subtask 1", retrievedSubtask.getName());
        assertEquals(Duration.ofMinutes(30), retrievedSubtask.getDuration());
        assertEquals(LocalDateTime.of(2025, 3, 1, 12, 0), retrievedSubtask.getStartTime());
    }

    @Test
    void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask(
                "Subtask 1", "Description", epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));
        int subtaskId = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getSubtaskById(subtaskId).isEmpty());
    }

    @Test
    void shouldNotCreateSubtaskWithTimeConflict() throws IOException, InterruptedException {
        manager.createSubtask(new Subtask(
                "Existing Subtask", "Description", epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));

        Subtask conflictingSubtask = new Subtask(
                "Conflicting Subtask", "Description", epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 15)
        );

        String jsonSubtask = gson.toJson(conflictingSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getSubtasks().size());
    }
}