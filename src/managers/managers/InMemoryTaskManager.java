package managers;

import tasks.*;

import java.util.*;

public class InMemoryTaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = new InMemoryHistoryManager();
    private int nextId = 1;

    private int generateId() {
        return nextId++;
    }

    // --- Tasks ---
    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    // --- Subtasks ---
    public void createSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
        }
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtasksIds().remove(Integer.valueOf(id));
            }
            subtasks.remove(id);
            historyManager.remove(id);
        }
    }

    // --- Epics ---
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int subtaskId : new ArrayList<>(epic.getSubtasksIds())) {
                deleteSubtaskById(subtaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
        }
    }

    // Для Main
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
