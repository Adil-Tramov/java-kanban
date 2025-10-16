package test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    public static void main(String[] args) {
        FileBackedTaskManagerTest test = new FileBackedTaskManagerTest();
        test.setUp();
        test.runAllTests();
    }

    public void setUp() {
        try {
            File tempFile = File.createTempFile("tasks", ".csv");
            tempFile.deleteOnExit();
            manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("FAIL: Could not create temp file");
        }
    }

    public void runAllTests() {
        testEpicStatusCalculation();
        testTaskCrossing();
        testTaskNotCrossing();
        testEpicSubtasks();
        testSaveAndLoad();
    }

    public void testSaveAndLoad() {
        Task task1 = new Task(1, "Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task1);

        File tempFile = new File(((FileBackedTaskManager) manager).filePath);
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile.getAbsolutePath());

        if (loaded.getAllTasks().size() != 1 || !loaded.getAllTasks().get(0).getName().equals("Task1")) {
            System.out.println("FAIL: Load from file failed");
        } else {
            System.out.println("PASS: Load from file succeeded");
        }
    }
}
