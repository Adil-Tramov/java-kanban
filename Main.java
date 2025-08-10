public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int task1Id = manager.createTask(task1);

        Epic epic1 = new Epic("Epic 1", "Epic description");
        int epic1Id = manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Sub description", TaskStatus.NEW, epic1Id);
        int subtask1Id = manager.createSubtask(subtask1);

        // Получаем задачи
        Task savedTask = manager.getTaskById(task1Id);
        Epic savedEpic = manager.getEpicById(epic1Id);
        Subtask savedSubtask = manager.getSubtaskById(subtask1Id);

        // Печатаем историю
        System.out.println("History:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}