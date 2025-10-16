import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Deque<Task> history = new ArrayDeque<>();
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        if (nodeMap.containsKey(task.getId())) {
            remove(task.getId());
        }

        Node node = new Node(task);
        nodeMap.put(task.getId(), node);
        history.addLast(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    private void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            history.remove(node.task);
        }
    }
}
