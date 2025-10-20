package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.managers.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.managers.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
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

public class HttpTaskManagerHistoryTest {

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
    public void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(5),
                LocalDateTime.now());
        Epic epic = new Epic("Epic 1", "Desc 1");
        Subtask subtask = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        // Вызываем GET для добавления в историю
        HttpClient client = HttpClient.newHttpClient();
        URI url1 = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).GET().build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI url2 = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        URI url3 = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request3 = HttpRequest.newBuilder().uri(url3).GET().build();
        client.send(request3, HttpResponse.BodyHandlers.ofString());

        URI urlHistory = URI.create("http://localhost:8080/history");
        HttpRequest requestHistory = HttpRequest.newBuilder().uri(urlHistory).GET().build();

        HttpResponse<String> response = client.send(requestHistory, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), List.class);
        assertNotNull(history);
        assertEquals(3, history.size()); // task, epic, subtask
    }
}