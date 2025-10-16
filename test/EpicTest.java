import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void testEpicInitialStatus() {
        Epic epic = new Epic("E", "D");
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void testEpicUpdateWithSubtasks() {
        Epic epic = new Epic("E", "D");
        List<Subtask> subtasks = new ArrayList<>();

        Subtask s1 = new Subtask("S1", "D1", 1);
        s1.setStartTime(LocalDateTime.of(2025, 10, 17, 10, 0));
        s1.setDuration(Duration.ofMinutes(30));
        s1.setStatus(TaskStatus.DONE);

        Subtask s2 = new Subtask("S2", "D2", 1);
        s2.setStartTime(LocalDateTime.of(2025, 10, 17, 11, 0));
        s2.setDuration(Duration.ofMinutes(60));
        s2.setStatus(TaskStatus.NEW);

        subtasks.add(s1);
        subtasks.add(s2);

        epic.updateTimesAndDuration(subtasks);

        assertEquals(LocalDateTime.of(2025, 10, 17, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 10, 17, 12, 0), epic.getEndTime());
        assertEquals(Duration.ofMinutes(120), epic.getDuration());
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void testEpicWithNoSubtasks() {
        Epic epic = new Epic("E", "D");
        epic.updateTimesAndDuration(new ArrayList<>());

        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
        assertEquals(Duration.ZERO, epic.getDuration());
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }
}