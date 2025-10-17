import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    private int nextId = 1;

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                    .thenComparing(Task::getId)
    );

    private void addTaskToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeTaskFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    private boolean isIntersecting(Task a, Task b) {
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
            return false;
        }

        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    private boolean isTaskIntersectsWithOthers(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(t -> !t.equals(task))
                .anyMatch(other -> isIntersecting(task, other));
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void createTask(Task task) {
        if (isTaskIntersectsWithOthers(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        addTaskToPrioritized(task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не найден");
        }
        if (isTaskIntersectsWithOthers(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpic(epic);
        addTaskToPrioritized(subtask);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (isTaskIntersectsWithOthers(updatedTask)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        Task oldTask = tasks.get(updatedTask.getId());
        if (oldTask != null) {
            removeTaskFromPrioritized(oldTask);
        }
        tasks.put(updatedTask.getId(), updatedTask);
        addTaskToPrioritized(updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        Epic storedEpic = epics.get(updatedEpic.getId());
        if (storedEpic != null) {
            List<Subtask> relatedSubtasks = getEpicSubtasks(updatedEpic.getId());
            updatedEpic.setSubtaskIds(storedEpic.getSubtaskIds());
            updatedEpic.updateTimesAndDuration(relatedSubtasks);
            epics.put(updatedEpic.getId(), updatedEpic);
        }
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (isTaskIntersectsWithOthers(updatedSubtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        Subtask oldSubtask = subtasks.get(updatedSubtask.getId());
        if (oldSubtask != null) {
            removeTaskFromPrioritized(oldSubtask);
        }
        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        Epic epic = epics.get(updatedSubtask.getEpicId());
        if (epic != null) {
            updateEpic(epic);
        }
        addTaskToPrioritized(updatedSubtask);
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeTaskFromPrioritized(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    removeTaskFromPrioritized(subtask);
                    historyManager.remove(subtaskId);
                }
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
                updateEpic(epic);
            }
            removeTaskFromPrioritized(subtask);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        prioritizedTasks.removeIf(t -> t instanceof Task && !(t instanceof Subtask));
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
        prioritizedTasks.removeIf(t -> t instanceof Subtask);
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.setSubtaskIds(new ArrayList<>());
            epic.updateTimesAndDuration(new ArrayList<>());
        }
        prioritizedTasks.removeIf(t -> t instanceof Subtask);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}