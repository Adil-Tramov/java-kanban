package test;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskTest {

    public static void main(String[] args) {
        TaskTest test = new TaskTest();
        test.testGetEndTime();
        test.testGetEndTimeWithNull();
    }

    public void testGetEndTime() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), now);
        if (!task.getEndTime().equals(now.plusMinutes(30))) {
            System.out.println("FAIL: getEndTime() returned wrong time");
        } else {
            System.out.println("PASS: getEndTime() works correctly");
        }
    }

    public void testGetEndTimeWithNull() {
        Task task = new Task(1, "Task1", "Desc1", TaskStatus.NEW, null, null);
        if (task.getEndTime() != null) {
            System.out.println("FAIL: getEndTime() should return null when startTime is null");
        } else {
            System.out.println("PASS: getEndTime() returns null when needed");
        }
    }
}
