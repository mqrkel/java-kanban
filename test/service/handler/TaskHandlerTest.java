package service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exceptions.NotFoundException;
import http.HttpTaskServer;
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

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT + "/tasks";

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
    void shouldReturnEmptyListWhenNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldUpdateTaskSuccessfully() throws IOException, InterruptedException {
        Task task = new Task(
                "Task 1",
                "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        );

        String jsonTask = gson.toJson(task);

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
        List<Task> tasks = gson.fromJson(getResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        int taskId = tasks.get(tasks.size() - 1).getId();

        Task taskForUpdate = new Task(
                taskId,
                "Updated Task 1",
                "Updated Description",
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
        List<Task> tasksAfterUpdate = gson.fromJson(getUpdatedResponse.body(), new TypeToken<List<Task>>() {
        }.getType());

        Task updatedTask = tasksAfterUpdate.stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Updated task not found"));

        assertEquals(taskId, updatedTask.getId());
        assertEquals("Updated Task 1", updatedTask.getName());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(Duration.ofMinutes(45), updatedTask.getDuration());
        assertEquals(LocalDateTime.of(2025, 3, 2, 12, 0), updatedTask.getStartTime());
    }

    @Test
    void shouldReturnCreatedTaskWhenAdded() throws IOException, InterruptedException {
        Task task = new Task(
                "Task 1",
                "Description",
                Duration.ofMinutes(5),
                LocalDateTime.of(2025, 3, 1, 12, 0));

        String jsonTask = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size());
        Task createdTask = tasks.get(0);

        assertNotNull(createdTask.getId());

        assertEquals("Task 1", createdTask.getName());
        assertEquals("Description", createdTask.getDescription());
        assertEquals(5, createdTask.getDuration().toMinutes());
        assertEquals("2025-03-01T12:00", createdTask.getStartTime().toString());
    }

    @Test
    void shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task(
                "Test Task",
                "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task returnedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), returnedTask.getId());
        assertEquals(task.getName(), returnedTask.getName());
        assertEquals(task.getDescription(), returnedTask.getDescription());
        assertEquals(task.getTaskType(), returnedTask.getTaskType());
        assertEquals(task.getDuration(), returnedTask.getDuration());
        assertEquals(task.getStartTime(), returnedTask.getStartTime());
    }

    @Test
    void shouldReturnNotFoundForNonexistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldNotCreateTaskWithTimeConflict() throws IOException, InterruptedException {
        manager.createTask(new Task(
                "Existing Task",
                "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));

        Task conflictingTask = new Task(
                "Conflicting Task",
                "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 15)
        );

        String jsonTask = gson.toJson(conflictingTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task(
                "Task to Delete",
                "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 3, 1, 12, 0)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertFalse(manager.getTaskById(task.getId()).isPresent());
    }

    @Test
    void shouldReturnNotFoundForDeletingNonexistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnTasks() throws IOException, InterruptedException {
        manager.createTask(new Task(
                "Tasks 1",
                "Desc",
                Duration.ofMinutes(5),
                LocalDateTime.of(2025, 3, 1, 12, 0)));
        manager.createTask(new Task(
                "Tasks 2",
                "Desc",
                Duration.ofMinutes(5),
                LocalDateTime.of(2025, 3, 1, 12, 10)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = manager.getTasks();
        assertEquals(2, tasks.size());
    }
}