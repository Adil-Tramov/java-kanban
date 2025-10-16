public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }
}