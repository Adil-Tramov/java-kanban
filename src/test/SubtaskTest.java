package test;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskTest {

    public static void main(String[] args) {
        SubtaskTest test = new SubtaskTest();
        test.testGetEpicId();
    }

    public void testGetEpicId() {
        Subtask subtask = new Subtask(1, "Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), 2);
        if (subtask.getEpicId() != 2) {
            System.out.println("FAIL: getEpicId() returned wrong value");
        } else {
            System.out.println("PASS: getEpicId() works correctly");
        }
    }
}