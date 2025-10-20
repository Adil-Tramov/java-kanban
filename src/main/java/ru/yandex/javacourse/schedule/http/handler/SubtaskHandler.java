package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.exceptions.IntersectionException;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.tasks.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
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
        if ("/subtasks".equals(path)) {
            List<Subtask> subtasks = manager.getSubtasks();
            try {
                sendText(exchange, gson.toJson(subtasks));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (path.matches("/subtasks/\\d+")) {
            int id = extractIdFromPath(path);
            try {
                Subtask subtask = manager.getSubtaskById(id);
                sendText(exchange, gson.toJson(subtask));
            } catch (NotFoundException e) {
                try {
                    sendNotFound(exchange, e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (path.matches("/subtasks/\\d+/subtasks")) {
            int id = extractIdFromPath(path);
            try {
                List<Subtask> subtasks = manager.getEpicSubtasks(id);
                sendText(exchange, gson.toJson(subtasks));
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
            Subtask subtask = gson.fromJson(reader, Subtask.class);
            if (subtask == null) {
                sendNotFound(exchange, "Тело запроса пустое или некорректное");
                return;
            }

            if (subtask.getId() == 0) {
                try {
                    manager.createSubtask(subtask);
                    sendCreated(exchange, gson.toJson(subtask));
                } catch (IntersectionException e) {
                    sendHasIntersections(exchange, e.getMessage());
                }
            } else {
                try {
                    manager.updateSubtask(subtask);
                    sendText(exchange, gson.toJson(subtask));
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
        if (path.matches("/subtasks/\\d+")) {
            int id = extractIdFromPath(path);
            try {
                manager.deleteSubtask(id);
                sendText(exchange, "Подзадача удалена");
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