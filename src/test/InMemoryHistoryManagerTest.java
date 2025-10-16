package test;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryHistoryManagerTest {

    public static void main(String[] args) {
        InMemoryHistoryManagerTest test = new InMemoryHistoryManagerTest();
        test.runAllTests();
    }

    public void runAllTests() {
        testAddTaskToHistory();
        testDuplicateInHistory();
        testEmptyHistory();
    }

    public void testAddTaskToHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        historyManager.add(task);

        if (historyManager.getHistory().size() != 1 || historyManager.getHistory().get(0).getId() != 1) {
            System.out.println("FAIL: Add task to history failed");
        } else {
            System.out.println("PASS: Add task to history succeeded");
        }
    }

    public void testDuplicateInHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        historyManager.add(task);
        historyManager.add(task);

        if (historyManager.getHistory().size() != 1) {
            System.out.println("FAIL: Duplicate in history not handled");
        } else {
            System.out.println("PASS: Duplicate in history handled");
        }
    }

    public void testEmptyHistory() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        if (!historyManager.getHistory().isEmpty()) {
            System.out.println("FAIL: History should be empty");
        } else {
            System.out.println("PASS: History is empty");
        }
    }
}
