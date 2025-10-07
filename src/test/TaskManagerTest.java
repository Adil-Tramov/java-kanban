package test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    public void testEpicStatusCalculation() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW, new ArrayList<>());
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), 1);
        manager.createSubtask(sub1);

        if (manager.calculateEpicStatus(1) != TaskStatus.NEW) {
            System.out.println("FAIL: Epic status should be NEW");
        } else {
            System.out.println("PASS: Epic status is NEW");
        }

        sub1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);

        if (manager.calculateEpicStatus(1) != TaskStatus.DONE) {
            System.out.println("FAIL: Epic status should be DONE");
        } else {
            System.out.println("PASS: Epic status is DONE");
        }
    }

    public void testTaskCrossing() {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(30),
                task1.getStartTime().plusMinutes(15));

        if (!manager.isTaskCrossedWith(task1, task2)) {
            System.out.println("FAIL: Tasks should be crossed");
        } else {
            System.out.println("PASS: Tasks are crossed");
        }

        if (!manager.isTaskCrossed(task2)) {
            System.out.println("FAIL: Task2 should be crossed with others");
        } else {
            System.out.println("PASS: Task2 is crossed");
        }
    }

    public void testTaskNotCrossing() {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(30),
                task1.getEndTime());

        if (manager.isTaskCrossedWith(task1, task2)) {
            System.out.println("FAIL: Tasks should NOT be crossed");
        } else {
            System.out.println("PASS: Tasks are not crossed");
        }
    }

    public void testEpicSubtasks() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW, new ArrayList<>());
        manager.createEpic(epic);

        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), 1);
        manager.createSubtask(sub1);

        if (manager.getEpicSubtasks(1).size() != 1 || manager.getEpicSubtasks(1).get(0).getId() != 2) {
            System.out.println("FAIL: Epic should have 1 subtask with id 2");
        } else {
            System.out.println("PASS: Epic has correct subtasks");
        }
    }
}