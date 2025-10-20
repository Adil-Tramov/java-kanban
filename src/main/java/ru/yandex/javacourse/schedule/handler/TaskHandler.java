package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.exceptions.IntersectionException;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.tasks.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();

            switch (method) {
                case "GET":
                    handleGet(path, exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(path, exchange);
                    break;
                default:
                    sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            try {
                sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleGet(String path, HttpExchange exchange) {
        if ("/tasks".equals(path)) {
            List<Task> tasks = manager.getTasks();
            try {
                sendText(exchange, gson.toJson(tasks));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (path.matches("/tasks/\\d+")) {
            int id = extractIdFromPath(path);
            try {
                Task task = manager.getTaskById(id);
                sendText(exchange, gson.toJson(task));
            } catch (NotFoundException e) {
                try {
                    sendNotFound(exchange, e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                sendNotFound(exchange, "Ресурс не найден");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handlePost(HttpExchange exchange) {
        try (Reader reader = new InputStreamReader(exchange.getRequestBody())) {
            Task task = gson.fromJson(reader, Task.class);
            if (task == null) {
                sendNotFound(exchange, "Тело запроса пустое или некорректное");
                return;
            }

            if (task.getId() == 0) {
                try {
                    manager.createTask(task);
                    sendCreated(exchange, gson.toJson(task));
                } catch (IntersectionException e) {
                    sendHasIntersections(exchange, e.getMessage());
                }
            } else {
                try {
                    manager.updateTask(task);
                    sendText(exchange, gson.toJson(task));
                } catch (NotFoundException e) {
                    sendNotFound(exchange, e.getMessage());
                } catch (IntersectionException e) {
                    sendHasIntersections(exchange, e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            try {
                sendNotFound(exchange, e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IntersectionException e) {
            try {
                sendHasIntersections(exchange, e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleDelete(String path, HttpExchange exchange) {
        if (path.matches("/tasks/\\d+")) {
            int id = extractIdFromPath(path);
            try {
                manager.deleteTask(id);
                sendText(exchange, "Задача удалена");
            } catch (NotFoundException e) {
                try {
                    sendNotFound(exchange, e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                sendNotFound(exchange, "Ресурс не найден");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}