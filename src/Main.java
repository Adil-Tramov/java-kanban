import managers.InMemoryTaskManager;
import tasks.*;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task t1 = new Task("T1", "Task one", Status.NEW);
        manager.createTask(t1);

        Epic epic = new Epic("Epic 1", "big feature");
        manager.createEpic(epic);

        Subtask s1 = new Subtask("Sub 1", "part 1", Status.NEW, epic.getId());
        manager.createSubtask(s1);

        // Просматривание задач (historyManager.add будет выполнен в getById)
        manager.getTaskById(t1.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(s1.getId());
        manager.getTaskById(t1.getId()); // второй просмотр — предыдущий из истории удалится

        System.out.println("History:");
        for (Task t : manager.getHistory()) {
            System.out.println(t);
        }
    }
}
