package test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicTest {

    public static void main(String[] args) {
        EpicTest test = new EpicTest();
        test.testGetDuration();
        test.testGetStartTime();
        test.testGetEndTime();
    }

    public void testGetDuration() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW, new ArrayList<>());
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), 1);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(30), 1);

        epic.getSubtaskIds().add(2);
        epic.getSubtaskIds().add(3);

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Duration expected = Duration.ofMinutes(50);
        Duration actual = epic.getDuration(manager);

        if (!expected.equals(actual)) {
            System.out.println("FAIL: Epic duration should be 50 minutes");
        } else {
            System.out.println("PASS: Epic duration is 50 minutes");
        }
    }

    public void testGetStartTime() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW, new ArrayList<>());
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), 1);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(10), 1);

        epic.getSubtaskIds().add(2);
        epic.getSubtaskIds().add(3);

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        LocalDateTime expected = sub1.getStartTime();
        LocalDateTime actual = epic.getStartTime(manager);

        if (!expected.equals(actual)) {
            System.out.println("FAIL: Epic start time should match earliest subtask");
        } else {
            System.out.println("PASS: Epic start time is correct");
        }
    }

    public void testGetEndTime() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW, new ArrayList<>());
        Subtask sub1 = new Subtask(2, "Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now(), 1);
        Subtask sub2 = new Subtask(3, "Sub2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(60), 1);

        epic.getSubtaskIds().add(2);
        epic.getSubtaskIds().add(3);

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        LocalDateTime expected = sub2.getEndTime();
        LocalDateTime actual = epic.getEndTime(manager);

        if (!expected.equals(actual)) {
            System.out.println("FAIL: Epic end time should match latest subtask");
        } else {
            System.out.println("PASS: Epic end time is correct");
        }
    }
}