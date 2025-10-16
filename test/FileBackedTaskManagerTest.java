import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    void setUp() {
        tempFile = new File("test_tasks.csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testSaveAndLoad() {
        Task task = new Task("Saved Task", "From file") {};
        task.setStartTime(LocalDateTime.of(2025, 10, 17, 14, 0));
        task.setDuration(Duration.ofMinutes(45));
        taskManager.createTask(task);

        Epic epic = new Epic("Saved Epic", "Epic from file");
        taskManager.createEpic(epic);

        Subtask sub = new Subtask("Saved Sub", "Sub from file", epic.getId());
        sub.setStartTime(LocalDateTime.of(2025, 10, 17, 15, 0));
        sub.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(sub);

        // Создаём новый менеджер — он загрузит из файла
        FileBackedTaskManager newManager = new FileBackedTaskManager(tempFile);

        Task loadedTask = newManager.getTaskById(task.getId());
        assertNotNull(loadedTask, "Задача не загрузилась из файла");
        assertEquals("Saved Task", loadedTask.getTitle());
        assertEquals(LocalDateTime.of(2025, 10, 17, 14, 0), loadedTask.getStartTime());
        assertEquals(Duration.ofMinutes(45), loadedTask.getDuration());

        Epic loadedEpic = newManager.getEpicById(epic.getId());
        assertNotNull(loadedEpic, "Эпик не загрузился из файла");
        assertEquals(1, loadedEpic.getSubtaskIds().size());

        Subtask loadedSub = newManager.getSubtaskById(sub.getId());
        assertNotNull(loadedSub, "Подзадача не загрузилась из файла");
        assertEquals(epic.getId(), loadedSub.getEpicId());
    }
}