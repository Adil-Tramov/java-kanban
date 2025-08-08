public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создаём задачи
        Task task1 = new Task("Покупки", "Купить продукты");
        Task task2 = new Task("Спорт", "Пойти в зал");
        manager.createTask(task1);
        manager.createTask(task2);

        // Создаём эпики
        Epic epic1 = new Epic("Переезд", "Переехать в новую квартиру");
        Epic epic2 = new Epic("Ремонт", "Сделать ремонт в ванной");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        // Подзадачи для эпика 1
        Subtask sub1 = new Subtask("Упаковать вещи", "Все коробки", epic1.getId());
        Subtask sub2 = new Subtask("Вызвать грузчиков", "Договориться на завтра", epic1.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        // Подзадача для эпика 2
        Subtask sub3 = new Subtask("Купить плитку", "Выбрать в магазине", epic2.getId());
        manager.createSubtask(sub3);

        // Выводим списки
        System.out.println("=== Все задачи ===");
        for (Task t : manager.getAllTasks()) {
            System.out.println(t);
        }

        System.out.println("\n=== Все эпики ===");
        for (Epic e : manager.getAllEpics()) {
            System.out.println(e);
        }

        System.out.println("\n=== Все подзадачи ===");
        for (Subtask s : manager.getAllSubtasks()) {
            System.out.println(s);
        }

        // Меняем статусы
        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);

        sub3.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub3);

        System.out.println("\n=== После обновления статусов ===");
        System.out.println("Эпик 1: " + manager.getEpicById(epic1.getId()));
        System.out.println("Эпик 2: " + manager.getEpicById(epic2.getId()));

        // Удаляем задачу и эпик
        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("\n=== После удаления ===");
        System.out.println("Оставшиеся задачи: " + manager.getAllTasks().size());
        System.out.println("Оставшиеся эпики: " + manager.getAllEpics().size());
        System.out.println("Оставшиеся подзадачи: " + manager.getAllSubtasks().size());
    }
}