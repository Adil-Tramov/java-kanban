package ru.yandex.javacourse.schedule.http.managers;

import ru.yandex.javacourse.schedule.http.tasks.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}