import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    protected final String filePath;

    public FileBackedTaskManager(String path) {
        this.filePath = path;
    }

    public static FileBackedTaskManager loadFromFile(String path) {
        FileBackedTaskManager manager = new FileBackedTaskManager(path);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            String header = reader.readLine();
            if (!header.equals("id,type,name,status,description,duration,startTime")) {
                return manager;
            }

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String name = parts[2];
                String status = parts[3];
                String description = parts[4];

                Duration duration = null;
                if (!parts[5].isEmpty()) {
                    duration = Duration.ofMinutes(Long.parseLong(parts[5]));
                }

                LocalDateTime startTime = null;
                if (!parts[6].isEmpty()) {
                    startTime = LocalDateTime.parse(parts[6], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }

                TaskStatus taskStatus = TaskStatus.valueOf(status);

                switch (type) {
                    case "TASK" -> {
                        Task task = new Task(id, name, description, taskStatus, duration, startTime);
                        manager.createTask(task);
                    }
                    case "SUBTASK" -> {
                        int epicId = Integer.parseInt(parts[7]);
                        Subtask subtask = new Subtask(id, name, description, taskStatus, duration, startTime, epicId);
                        manager.createSubtask(subtask);
                    }
                    case "EPIC" -> {
                        Epic epic = new Epic(id, name, description, taskStatus, new ArrayList<>());
                        manager.createEpic(epic);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке из файла", e);
        }

        return manager;
    }

    public void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("id,type,name,status,description,duration,startTime,epicId");

            for (Task task : getAllTasks()) {
                writeTask(writer, task, "TASK");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writeTask(writer, subtask, "SUBTASK");
            }
            for (Epic epic : getAllEpics()) {
                writeTask(writer, epic, "EPIC");
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении в файл", e);
        }
    }

    private void writeTask(PrintWriter writer, Task task, String type) {
        long duration = 0;
        if (task instanceof Epic) {
            duration = ((Epic) task).getDuration(this).toMinutes();
        } else {
            duration = task.getDuration() != null ? task.getDuration().toMinutes() : 0;
        }

        String startTime = task.getStartTime() != null ? task.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
        writer.printf("%d,%s,%s,%s,%s,%d,%s", task.getId(), type, task.getName(), task.getStatus(), task.getDescription(), duration, startTime);

        if (task instanceof Subtask) {
            writer.print("," + ((Subtask) task).getEpicId());
        }
        writer.println();
    }

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public Task updateTask(Task task) {
        Task updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        save();
        return created;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updated = super.updateSubtask(subtask);
        save();
        return updated;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updated = super.updateEpic(epic);
        save();
        return updated;
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }
}
