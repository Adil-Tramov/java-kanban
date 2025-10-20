package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacourse.schedule.http.handler.EpicHandler;
import ru.yandex.javacourse.schedule.http.handler.HistoryHandler;
import ru.yandex.javacourse.schedule.http.handler.PrioritizedHandler;
import ru.yandex.javacourse.schedule.http.handler.SubtaskHandler;
import ru.yandex.javacourse.schedule.http.handler.TaskHandler;
import ru.yandex.javacourse.schedule.http.managers.Managers;
import ru.yandex.javacourse.schedule.http.tasks.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            registerHandlers();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось запустить сервер на порту " + PORT, e);
        }
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    private void registerHandlers() {
        Gson gson = getGson();
        server.createContext("/tasks", new TaskHandler(manager, gson));
        server.createContext("/subtasks", new SubtaskHandler(manager, gson));
        server.createContext("/epics", new EpicHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    static class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
        @Override
        public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toMinutes());
        }

        @Override
        public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Duration.ofMinutes(json.getAsLong());
        }
    }

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}