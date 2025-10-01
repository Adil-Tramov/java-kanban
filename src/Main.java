import managers.InMemoryTaskManager;
import tasks.Task;
import tasks.Subtask;
import tasks.Epic;
import tasks.Status;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task t1 = new Task("T1", "Первая задача", Status.NEW);
        manager.createTask(t1);

        Epic epic = new Epic("Эпик 1", "важная функция");
        manager.createEpic(epic);

        Subtask s1 = new Subtask("Подзадача 1", "часть 1", Status.NEW, epic.getId());
        manager.createSubtask(s1);

        // Просмотр задач
        manager.getTaskById(t1.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(s1.getId());
        manager.getTaskById(t1.getId()); // повторный просмотр

        System.out.println("History:");
        for (Task t : manager.getHistory()) {
            System.out.println(t);
        }
    }
}

