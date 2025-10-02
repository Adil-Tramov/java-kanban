package managers;

import tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Task task) {
            this.task = task;
        }
    }

    private Node head;
    private Node tail;
    private final Map<Integer, Node> historyMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        int id = task.getId();
        // если уже есть — удаляем старый узел (O(1))
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
        }
        // добавляем новый узел в конец
        Node node = linkLast(task);
        historyMap.put(id, node);
    }

    private Node linkLast(Task task) {
        Node node = new Node(task);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        return node;
    }

    private void removeNode(Node node) {
        if (node == null) return;
        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }

        historyMap.remove(node.task.getId());
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }
}
