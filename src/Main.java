public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        // 1. Две обычные задачи
        var task1 = new Task("Купить молоко", "В магазине", Status.NEW);
        var task2 = new Task("Позвонить маме", "Поздравить", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // 2. Эпик с 3 подзадачами
        var epic1 = new Epic("Подготовить отчёт", "Собрать данные");
        taskManager.createEpic(epic1);

        var sub1 = new Subtask("Собрать данные", "Из базы", Status.NEW, epic1.getId());
        var sub2 = new Subtask("Построить графики", "В Excel", Status.NEW, epic1.getId());
        var sub3 = new Subtask("Написать выводы", "По анализу", Status.NEW, epic1.getId());
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        taskManager.createSubtask(sub3);

        // 3. Эпик без подзадач
        var epic2 = new Epic("Пустой эпик", "Нет подзадач");
        taskManager.createEpic(epic2);

        System.out.println("=== Сценарий начался ===\n");

        // Запросы
        taskManager.getTaskById(task1.getId());
        printHistory(taskManager);

        taskManager.getEpicById(epic1.getId());
        printHistory(taskManager);

        taskManager.getTaskById(task2.getId());
        printHistory(taskManager);

        taskManager.getSubtaskById(sub1.getId());
        printHistory(taskManager);

        // Повторный запрос — должен переместиться в конец
        taskManager.getTaskById(task1.getId());
        printHistory(taskManager);

        // Удаление задачи
        System.out.println("\n→ Удаляем задачу 'Позвонить маме'");
        taskManager.deleteTaskById(task2.getId());
        printHistory(taskManager);

        // Удаление эпика с подзадачами
        System.out.println("\n→ Удаляем эпик 'Подготовить отчёт' (и все подзадачи)");
        taskManager.deleteEpicById(epic1.getId());
        printHistory(taskManager);

        System.out.println("\n=== Сценарий завершён ===");
    }

    private static void printHistory(InMemoryTaskManager tm) {
        var history = tm.getHistory();
        System.out.println("История (" + history.size() + "):");
        for (var t : history) {
            System.out.println("  " + t);
        }
    }
}
