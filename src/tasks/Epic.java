package tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtasksIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void addSubtaskId(int id) {
        subtasksIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtasksIds.remove(Integer.valueOf(id));
    }

    public void clearSubtasks() {
        subtasksIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", subtasksIds=" + subtasksIds +
                '}';
    }
}
