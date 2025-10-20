package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import main.java.ru.yandex.javacourse.schedule.http.exceptions.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import main.java.ru.yandex.javacourse.schedule.http.managers.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.managers.TaskManager;
import main.java.ru.yandex.javacourse.schedule.http.tasks.Epic;
import main.java.ru.yandex.javacourse.schedule.http.tasks.Subtask;
import main.java.ru.yandex.javacourse.schedule.http.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpTaskManagerSubtasksTest {

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
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic for Subtask", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Subtask 1", subtasks.get(0).getName());
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc 1");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(10), epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), List.class);
        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc 1");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Single Subtask", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), receivedSubtask.getId());
        assertEquals(subtask.getName(), receivedSubtask.getName());
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc 1");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Old Name", "Old Desc", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask);

        subtask.setName("Updated Name");
        subtask.setDescription("Updated Desc");
        String updatedJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask updatedSubtask = manager.getSubtaskById(subtask.getId());
        assertEquals("Updated Name", updatedSubtask.getName());
        assertEquals("Updated Desc", updatedSubtask.getDescription());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc 1");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("To Delete", "Will be deleted", TaskStatus.NEW,
                Duration.ofMinutes(5), LocalDateTime.now(), epic.getId());
        manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class,
                () -> manager.getSubtaskById(subtask.getId()));
    }
}