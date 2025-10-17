import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void testAddAndRemove() {
        Task t1 = new Task("T1", "D1") {}; t1.setId(1);
        Task t2 = new Task("T2", "D2") {}; t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);
        assertEquals(2, historyManager.getHistory().size());

        historyManager.remove(1);
        var hist = historyManager.getHistory();
        assertEquals(1, hist.size());
        assertEquals(2, hist.get(0).getId());
    }

    @Test
    void testNoDuplicates() {
        Task t = new Task("T", "D") {}; t.setId(1);
        historyManager.add(t);
        historyManager.add(t);
        assertEquals(1, historyManager.getHistory().size());
    }
}