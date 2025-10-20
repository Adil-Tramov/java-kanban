package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.managers.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.managers.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerEpicsTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("New Epic", "Description");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size());
        assertEquals("New Epic", epics.get(0).getName());
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Desc 1");
        Epic epic2 = new Epic("Epic 2", "Desc 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epics = gson.fromJson(response.body(), List.class);
        assertNotNull(epics);
        assertEquals(2, epics.size());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Single Epic", "Desc");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), receivedEpic.getId());
        assertEquals(epic.getName(), receivedEpic.getName());
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Old Name", "Old Desc");
        manager.createEpic(epic);

        epic.setName("Updated Name");
        epic.setDescription("Updated Desc");
        String updatedJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals("Updated Name", updatedEpic.getName());
        assertEquals("Updated Desc", updatedEpic.getDescription());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("To Delete", "Will be deleted");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class,
                () -> manager.getEpicById(epic.getId()));
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with Subtasks", "Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(10), epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), List.class);
        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}