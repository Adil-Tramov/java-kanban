package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.HttpTaskServer;
import ru.yandex.javacourse.schedule.http.managers.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.http.tasks.Epic;
import ru.yandex.javacourse.schedule.http.tasks.Subtask;
import ru.yandex.javacourse.schedule.http.tasks.TaskManager;
import ru.yandex.javacourse.schedule.http.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerSubtasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerSubtasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
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
        Epic epic = new Epic("Epic for Subtask", "Description", TaskStatus.NEW);
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }
}