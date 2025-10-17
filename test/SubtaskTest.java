import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    void testSubtaskCreation() {
        Subtask sub = new Subtask("Sub", "Desc", 5);
        assertEquals(5, sub.getEpicId());
        assertEquals("Sub", sub.getTitle());
        assertEquals(TaskStatus.NEW, sub.getStatus());
    }

    @Test
    void testSubtaskToString() {
        Subtask sub = new Subtask("Test", "Desc", 10);
        sub.setId(1);
        String str = sub.toString();
        assertTrue(str.contains("Subtask"));
        assertTrue(str.contains("epicId=10"));
    }
}