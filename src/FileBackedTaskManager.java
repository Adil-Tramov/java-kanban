import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        if (file.exists() && file.length() > 0) {
            loadFromFile();
        }
    }

    private void loadFromFile() {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty() || lines.size() < 2) return;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 8) continue;

                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String title = parts[2];
                TaskStatus status = TaskStatus.valueOf(parts[3]);
                String description = parts[4];
                String durationStr = parts[5];
                String startTimeStr = parts[6];
                String epicIdStr = parts[7];

                Duration duration = "null".equals(durationStr) ? null : Duration.ofMinutes(Long.parseLong(durationStr));
                LocalDateTime startTime = "null".equals(startTimeStr) ? null : LocalDateTime.parse(startTimeStr);

                switch (type) {
                    case "TASK":
                        Task task = new Task(title, description) {};
                        task.setId(id);
                        task.setStatus(status);
                        task.setDuration(duration);
                        task.setStartTime(startTime);
                        tasks.put(id, task);
                        if (startTime != null) prioritizedTasks.add(task);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(title, description);
                        epic.setId(id);
                        epic.setStatus(status);
                        epics.put(id, epic);
                        break;
                    case "SUBTASK":
                        int epicId = "null".equals(epicIdStr) ? -1 : Integer.parseInt(epicIdStr);
                        Subtask sub = new Subtask(title, description, epicId);
                        sub.setId(id);
                        sub.setStatus(status);
                        sub.setDuration(duration);
                        sub.setStartTime(startTime);
                        subtasks.put(id, sub);
                        if (startTime != null) prioritizedTasks.add(sub);
                        Epic e = epics.get(epicId);
                        if (e != null) e.addSubtaskId(id);
                        break;
                }
            }

            // Обновляем эпики после загрузки подзадач
            for (Epic epic : epics.values()) {
                List<Subtask> subs = getEpicSubtasks(epic.getId());
                epic.updateTimesAndDuration(subs);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки из файла", e);
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            writer.write("id,type,title,status,description,duration,startTime,epic");
            writer.newLine();

            for (Task task : tasks.values()) {
                writer.write(serializeTask(task, "TASK", -1));
                writer.newLine();
            }
            for (Epic epic : epics.values()) {
                writer.write(serializeTask(epic, "EPIC", -1));
                writer.newLine();
            }
            for (Subtask sub : subtasks.values()) {
                writer.write(serializeTask(sub, "SUBTASK", sub.getEpicId()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения в файл", e);
        }
    }

    private String serializeTask(Task task, String type, int epicId) {
        String durationStr = task.getDuration() == null ? "null" : String.valueOf(task.getDuration().toMinutes());
        String startTimeStr = task.getStartTime() == null ? "null" : task.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String epicPart = type.equals("SUBTASK") ? "," + epicId : "";
        return task.getId() + "," + type + "," + task.getTitle() + "," + task.getStatus() + "," +
                task.getDescription() + "," + durationStr + "," + startTimeStr + epicPart;
    }

    // Переопределяем методы, чтобы вызывать save()
    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }
}