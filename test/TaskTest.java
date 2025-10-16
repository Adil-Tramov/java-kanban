import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void testGetEndTime_WhenStartTimeAndDurationSet() {
        Task task = new Task("T", "D") {};
        LocalDateTime start = LocalDateTime.of(2025, 10, 17, 10, 0);
        Duration dur = Duration.ofMinutes(90);
        task.setStartTime(start);
        task.setDuration(dur);

        assertEquals(LocalDateTime.of(2025, 10, 17, 11, 30), task.getEndTime());
    }

    @Test
    void testGetEndTime_WhenStartTimeIsNull() {
        Task task = new Task("T", "D") {};
        task.setDuration(Duration.ofMinutes(60));
        assertNull(task.getEndTime());
    }

    @Test
    void testGetEndTime_WhenDurationIsNull() {
        Task task = new Task("T", "D") {};
        task.setStartTime(LocalDateTime.now());
        assertNull(task.getEndTime());
    }

    @Test
    void testEqualsAndHashCode() {
        Task t1 = new Task("T", "D") {}; t1.setId(1);
        Task t2 = new Task("X", "Y") {}; t2.setId(1);
        Task t3 = new Task("A", "B") {}; t3.setId(2);

        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}