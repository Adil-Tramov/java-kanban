package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.HttpTaskServer;
import ru.yandex.javacourse.schedule.http.managers.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.http.tasks.Task;
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

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerPrioritizedTest() throws IOException {
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
    public void testGetPrioritized() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(5),
                LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW, Duration.ofMinutes(10),
                LocalDateTime.now());
        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), List.class);
        assertEquals(2, prioritized.size(), "Некорректное количество задач в приоритете");
        assertEquals("Task 2", prioritized.get(0).getName(), "Некорректная сортировка по приоритету");
    }
}