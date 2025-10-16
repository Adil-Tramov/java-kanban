package test;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    public static void main(String[] args) {
        InMemoryTaskManagerTest test = new InMemoryTaskManagerTest();
        test.setUp();
        test.runAllTests();
    }

    public void setUp() {
        manager = new InMemoryTaskManager();
    }

    public void runAllTests() {
        testEpicStatusCalculation();
        testTaskCrossing();
        testTaskNotCrossing();
        testEpicSubtasks();
        testPrioritizedTasks();
    }

    public void testPrioritizedTasks() {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(2, "Task2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(60));

        manager.createTask(task1);
        manager.createTask(task2);

        var prioritized = manager.getPrioritizedTasks();
        if (prioritized.size() != 2 || prioritized.get(0).getId() != 1 || prioritized.get(1).getId() != 2) {
            System.out.println("FAIL: Prioritized tasks are incorrect");
        } else {
            System.out.println("PASS: Prioritized tasks are correct");
        }
    }
}