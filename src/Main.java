<<<<<<< HEAD
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        FileBackedTaskManager manager = new FileBackedTaskManager("tasks.csv");

        // Создаём задачи
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task1);

        Epic epic1 = new Epic(2, "Epic 1", "Epic Description", TaskStatus.NEW, new ArrayList<>());
        manager.createEpic(epic1);

        Subtask sub1 = new Subtask(3, "Sub 1", "Sub Description", TaskStatus.NEW, Duration.ofMinutes(20),
                LocalDateTime.now().plusMinutes(30), 2);
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask(4, "Sub 2", "Sub 2 Description", TaskStatus.NEW, Duration.ofMinutes(25),
                LocalDateTime.now().plusMinutes(60), 2);
        manager.createSubtask(sub2);

        // Выводим приоритетные задачи
        System.out.println("Приоритетные задачи:");
        for (Task t : manager.getPrioritizedTasks()) {
            System.out.println(t.getName() + " - " + t.getStartTime());
        }

        // Проверяем пересечения
        System.out.println("Пересекаются ли задачи? " + manager.isTaskCrossed(sub1));

        // Статус эпика
        System.out.println("Статус эпика: " + manager.calculateEpicStatus(2));

        // Добавляем ещё одну задачу, пересекающуюся с sub1
        Task task2 = new Task(5, "Task 2", "Another task", TaskStatus.NEW, Duration.ofMinutes(15),
                sub1.getStartTime().plusMinutes(10));
        manager.createTask(task2);

        System.out.println("Пересекается ли task2 с другими? " + manager.isTaskCrossed(task2));
=======
public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
>>>>>>> upstream/main
    }
}
