package ru.yandex.javacourse.schedule.managers;

import ru.yandex.javacourse.schedule.tasks.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}