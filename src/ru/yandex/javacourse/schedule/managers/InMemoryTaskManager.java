package ru.yandex.javacourse.schedule.http.managers;

import ru.yandex.javacourse.schedule.http.exceptions.IntersectionException;
import ru.yandex.javacourse.schedule.http.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.http.tasks.Epic;
import ru.yandex.javacourse.schedule.http.tasks.Subtask;
import ru.yandex.javacourse.schedule.http.tasks.Task;
import ru.yandex.javacourse.schedule.http.tasks.TaskManager;
import ru.yandex.javacourse.schedule.http.tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();

    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime().isBefore(t2.getStartTime())) {
            return -1;
        } else if (t1.getStartTime().isAfter(t2.getStartTime())) {
            return 1;
        } else {
            return Integer.compare(t1.getId(), t2.getId());
        }
    });

    protected int nextId = 1;

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с id=" + id + " не найдена");
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с id=" + id + " не найдена");
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + id + " не найден");
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void createTask(Task task) throws IntersectionException {
        validateIntersection(task);
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        addToPrioritized(task);
    }

    @Override
    public void createSubtask(Subtask subtask) throws IntersectionException {
        validateIntersection(subtask);
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        addToPrioritized(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(epic);
        }
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    // Обновление
    @Override
    public void updateTask(Task task) throws IntersectionException {
        if (!tasks.containsKey(task.getId())) {
            throw new NotFoundException("Задача с id=" + task.getId() + " не найдена");
        }
        validateIntersection(task);
        tasks.put(task.getId(), task);
        removeFromPrioritized(task.getId());
        addToPrioritized(task);
    }

    @Override
    public void updateSubtask(Subtask subtask) throws IntersectionException {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NotFoundException("Подзадача с id=" + subtask.getId() + " не найдена");
        }
        validateIntersection(subtask);
        subtasks.put(subtask.getId(), subtask);
        removeFromPrioritized(subtask.getId());
        addToPrioritized(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NotFoundException("Эпик с id=" + epic.getId() + " не найден");
        }
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritized(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removeFromPrioritized(id);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
                removeFromPrioritized(subId);
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + epicId + " не найден");
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
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

    @Override
    public Set<Task> getIntersectionTasks(LocalDateTime start, LocalDateTime end) {
        Set<Task> intersecting = new java.util.HashSet<>();
        for (Task task : prioritizedTasks) {
            if (task.getStartTime() == null || task.getDuration() == null) continue;
            LocalDateTime taskStart = task.getStartTime();
            LocalDateTime taskEnd = task.getEndTime();
            if (!(taskEnd.isBefore(start) || taskStart.isAfter(end))) {
                intersecting.add(task);
            }
        }
        return intersecting;
    }

    private void validateIntersection(Task task) throws IntersectionException {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return;
        }
        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();
        for (Task existing : prioritizedTasks) {
            if (existing.getId() == task.getId()) continue;
            if (existing.getStartTime() == null || existing.getDuration() == null) continue;
            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEnd = existing.getEndTime();
            if (!(end.isBefore(existingStart) || start.isAfter(existingEnd))) {
                throw new IntersectionException("Задача пересекается с задачей id=" + existing.getId());
            }
        }
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> subtaskList = getEpicSubtasks(epic.getId());
        if (subtaskList.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (subtaskList.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE)) {
            epic.setStatus(TaskStatus.DONE);
        } else if (subtaskList.stream().anyMatch(s -> s.getStatus() == TaskStatus.IN_PROGRESS)) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(int id) {
        prioritizedTasks.removeIf(task -> task.getId() == id);
    }
}
