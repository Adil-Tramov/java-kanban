import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTimesAndDuration(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
            updateStatusBasedOnSubtasks(subtasks);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;

        for (Subtask sub : subtasks) {
            if (sub.getStartTime() != null) {
                if (earliestStart == null || sub.getStartTime().isBefore(earliestStart)) {
                    earliestStart = sub.getStartTime();
                }
            }
            if (sub.getEndTime() != null) {
                if (latestEnd == null || sub.getEndTime().isAfter(latestEnd)) {
                    latestEnd = sub.getEndTime();
                }
            }
        }

        this.startTime = earliestStart;
        this.endTime = latestEnd;

        if (earliestStart != null && latestEnd != null) {
            this.duration = Duration.between(earliestStart, latestEnd);
        } else {
            this.duration = null;
        }

        updateStatusBasedOnSubtasks(subtasks);
    }

    private void updateStatusBasedOnSubtasks(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            status = TaskStatus.NEW;
            return;
        }

        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasInProgress = false;

        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
            }
        }

        if (hasInProgress) {
            status = TaskStatus.IN_PROGRESS;
        } else if (hasNew && !hasDone) {
            status = TaskStatus.NEW;
        } else if (hasDone && !hasNew) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
    }
}