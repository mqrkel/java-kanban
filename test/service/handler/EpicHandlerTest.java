package service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.HttpTaskServer;
import model.Epic;
import model.Subtask;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest {
    private static final int PORT = HttpTaskServer.PORT;
    private static final String BASE_URL = "http://localhost:" + PORT + "/epics";

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
    void shouldCreateEpicSuccessfully() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description of epic");
        String jsonEpic = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        List<Epic> epics = gson.fromJson(getResponse.body(), new TypeToken<List<Epic>>() {
        }.getType());

        assertEquals(1, epics.size());
        assertEquals("Epic 1", epics.get(0).getName());
    }

    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic 1", "First epic"));
        manager.createEpic(new Epic("Epic 2", "Second epic"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());

        assertEquals(2, epics.size());
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        int epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertNotNull(retrievedEpic);
        assertEquals(epicId, retrievedEpic.getId());
        assertEquals("Epic 1", retrievedEpic.getName());
    }

    @Test
    void shouldReturn404ForNonExistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldDeleteEpicSuccessfully() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        int epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void shouldReturnAllSubtasksForEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        Integer epicId = epic.getId();

        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description", epicId));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description", epicId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epicId + "/" + "subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        TypeToken<List<Subtask>> listType = new TypeToken<>() {
        };
        List<Subtask> subtasks = gson.fromJson(response.body(), listType);
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));

    }
}