import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task = new Task("Test", "Desc", Status.NEW);
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = loaded.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals(task.getName(), tasks.get(0).getName());
    }

    @Test
    void shouldLoadEmptyFile() throws IOException {
        File tempFile = File.createTempFile("empty", ".csv");
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadSubtasksWithEpic() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, epic.getId());
        manager.createSubtask(sub);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());
        assertEquals(epic.getId(), loaded.getAllSubtasks().get(0).getEpicId());
    }
}