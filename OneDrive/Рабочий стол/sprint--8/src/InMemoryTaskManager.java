import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null || t2.getStartTime() == null) {
            return 0;
        }
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        prioritizedTasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Task createTask(Task task) {
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        Task old = tasks.get(task.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old);
            }
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
        }
        prioritizedTasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtaskIds().add(subtask.getId());
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask old = subtasks.get(subtask.getId());
        if (old != null) {
            if (old.getStartTime() != null) {
                prioritizedTasks.remove(old);
            }
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
        }
        subtasks.put(subtask.getId(), subtask);
        return subtask;
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
            }
            prioritizedTasks.remove(subtask);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
        prioritizedTasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epic.getSubtaskIds().clear();
            prioritizedTasks.removeAll(subtasks.values());
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return epic.getSubtaskIds().stream()
                    .map(subtasks::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void addSubtaskToEpic(int epicId, int subtaskId) {
        Epic epic = epics.get(epicId);
        Subtask subtask = subtasks.get(subtaskId);
        if (epic != null && subtask != null) {
            epic.getSubtaskIds().add(subtaskId);
            subtask.setEpicId(epicId);
        }
    }

    @Override
    public TaskStatus calculateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null || epic.getSubtaskIds().isEmpty()) {
            return TaskStatus.NEW;
        }

        long newCount = 0;
        long doneCount = 0;
        long inProgressCount = 0;

        for (int id : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                switch (subtask.getStatus()) {
                    case NEW -> newCount++;
                    case DONE -> doneCount++;
                    case IN_PROGRESS -> inProgressCount++;
                }
            }
        }

        if (doneCount == epic.getSubtaskIds().size()) {
            return TaskStatus.DONE;
        } else if (newCount == epic.getSubtaskIds().size()) {
            return TaskStatus.NEW;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public boolean isTaskCrossed(Task task) {
        if (task.getStartTime() == null) return false;

        return prioritizedTasks.stream()
                .anyMatch(other -> isTaskCrossedWith(task, other));
    }

    @Override
    public boolean isTaskCrossedWith(Task task, Task other) {
        if (task.getStartTime() == null || other.getStartTime() == null) return false;

        LocalDateTime start1 = task.getStartTime();
        LocalDateTime end1 = task.getEndTime();
        LocalDateTime start2 = other.getStartTime();
        LocalDateTime end2 = other.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
