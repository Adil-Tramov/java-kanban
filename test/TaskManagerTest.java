import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    abstract void setUp();

    @Test
    void testCreateAndGetTask() {
        Task task = new Task("Task 1", "Description 1") {};
        task.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 0));
        task.setDuration(Duration.ofMinutes(60));

        taskManager.createTask(task);
        Task retrieved = taskManager.getTaskById(task.getId());

        assertNotNull(retrieved);
        assertEquals("Task 1", retrieved.getTitle());
        assertEquals(LocalDateTime.of(2025, 10, 17, 10, 0), retrieved.getStartTime());
        assertEquals(Duration.ofMinutes(60), retrieved.getDuration());
        assertEquals(LocalDateTime.of(2025, 10, 17, 11, 0), retrieved.getEndTime());
    }

    @Test
    void testCreateEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask sub = new Subtask("Sub", "Desc", epic.getId());
        sub.setStartTime(LocalDateTime.of(2025, 10, 17, 9, 0));
        sub.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(sub);

        Epic retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(1, retrievedEpic.getSubtaskIds().size());
        assertEquals(LocalDateTime.of(2025, 10, 17, 9, 0), retrievedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 10, 17, 9, 30), retrievedEpic.getEndTime());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }

    @Test
    void testEpicStatus_AllDone() {
        Epic epic = new Epic("E", "D");
        taskManager.createEpic(epic);

        Subtask s1 = new Subtask("S1", "D1", epic.getId());
        s1.setStatus(TaskStatus.DONE);
        Subtask s2 = new Subtask("S2", "D2", epic.getId());
        s2.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(s1);
        taskManager.createSubtask(s2);

        Epic e = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, e.getStatus());
    }

    @Test
    void testEpicStatus_InProgress() {
        Epic epic = new Epic("E", "D");
        taskManager.createEpic(epic);

        Subtask s1 = new Subtask("S1", "D1", epic.getId());
        s1.setStatus(TaskStatus.NEW);
        Subtask s2 = new Subtask("S2", "D2", epic.getId());
        s2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.createSubtask(s1);
        taskManager.createSubtask(s2);

        Epic e = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, e.getStatus());
    }

    @Test
    void testGetPrioritizedTasks() {
        Task t1 = new Task("T1", "D1") {};
        t1.setStartTime(LocalDateTime.of(2025, 10, 17, 11, 0));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task("T2", "D2") {};
        t2.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 0));
        t2.setDuration(Duration.ofMinutes(30));

        taskManager.createTask(t1);
        taskManager.createTask(t2);

        List<Task> list = taskManager.getPrioritizedTasks();
        assertEquals(2, list.size());
        assertEquals(t2.getId(), list.get(0).getId());
        assertEquals(t1.getId(), list.get(1).getId());
    }

    @Test
    void testTaskIntersection() {
        Task t1 = new Task("T1", "D1") {};
        t1.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));

        taskManager.createTask(t1);

        Task t2 = new Task("T2", "D2") {};
        t2.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 30));
        t2.setDuration(Duration.ofMinutes(60));

        assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(t2));
    }
}