import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Купить продукты", "Сходить в магазин") {};
        task1.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 0));
        task1.setDuration(Duration.ofMinutes(45));
        manager.createTask(task1);

        Epic epic = new Epic("Подготовка к отпуску", "Собрать документы, купить билеты и т.д.");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Купить билеты", "На самолёт в Сочи", epic.getId());
        sub1.setStartTime(LocalDateTime.of(2025, 10, 17, 11, 0));
        sub1.setDuration(Duration.ofMinutes(30));
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask("Собрать чемодан", "Положить вещи", epic.getId());
        sub2.setStartTime(LocalDateTime.of(2025, 10, 17, 12, 0));
        sub2.setDuration(Duration.ofMinutes(60));
        manager.createSubtask(sub2);

        System.out.println("=== Задачи по приоритету (по времени начала) ===");
        List<Task> prioritized = manager.getPrioritizedTasks();
        for (Task t : prioritized) {
            System.out.println(t);
        }

        Epic updatedEpic = manager.getEpicById(epic.getId());
        System.out.println("\n=== Обновлённый эпик ===");
        System.out.println("Старт: " + updatedEpic.getStartTime());
        System.out.println("Окончание: " + updatedEpic.getEndTime());
        System.out.println("Продолжительность (мин): " + (updatedEpic.getDuration() != null ? updatedEpic.getDuration().toMinutes() : "null"));
        System.out.println("Статус: " + updatedEpic.getStatus());

        manager.getTaskById(task1.getId());
        manager.getSubtaskById(sub1.getId());
        manager.getEpicById(epic.getId());

        System.out.println("\n=== История просмотров ===");
        List<Task> history = manager.getHistory();
        for (Task t : history) {
            System.out.println(t.getTitle());
        }

        try {
            Task bad = new Task("Невозможная задача", "Пересекается") {}; // ← исправлено
            bad.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 30));
            bad.setDuration(Duration.ofMinutes(30));
            manager.createTask(bad);
        } catch (IllegalArgumentException e) {
            System.out.println("\nОшибка: " + e.getMessage());
        }
    }
}