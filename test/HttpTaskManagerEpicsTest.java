package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.HttpTaskServer;
import ru.yandex.javacourse.schedule.http.managers.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.http.tasks.Epic;
import ru.yandex.javacourse.schedule.http.tasks.TaskManager;
import ru.yandex.javacourse.schedule.http.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerEpicsTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("New Epic", "Description", TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode()); // Изменено с 200 на 201

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("New Epic", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }
}