import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(int id, String name, String description, TaskStatus status, List<Integer> subtaskIds) {
        super(id, name, description, status, null, null);
        this.subtaskIds = subtaskIds;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public Duration getDuration(TaskManager manager) {
        Duration total = Duration.ZERO;
        for (Integer id : subtaskIds) {
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask != null) {
                total = total.plus(subtask.getDuration());
            }
        }
        return total;
    }

    public LocalDateTime getStartTime(TaskManager manager) {
        LocalDateTime earliest = null;
        for (Integer id : subtaskIds) {
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask != null && (earliest == null || subtask.getStartTime().isBefore(earliest))) {
                earliest = subtask.getStartTime();
            }
        }
        return earliest;
    }

    public LocalDateTime getEndTime(TaskManager manager) {
        LocalDateTime latest = null;
        for (Integer id : subtaskIds) {
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask != null && (latest == null || subtask.getEndTime().isAfter(latest))) {
                latest = subtask.getEndTime();
            }
        }
        return latest;
    }
}