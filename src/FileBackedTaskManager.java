import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        loadFromFile();
    }

    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
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

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(taskToString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(taskToString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(taskToString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String taskToString(Task task) {
        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }
        return String.join(",",
                String.valueOf(task.getId()),
                getTaskType(task).name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
        );
    }

    private TaskType getTaskType(Task task) {
        if (task instanceof Subtask) return TaskType.SUBTASK;
        if (task instanceof Epic) return TaskType.EPIC;
        return TaskType.TASK;
    }

    private void loadFromFile() {
        if (!file.exists()) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) {
                return;
            }

            lines.remove(0);

            for (String line : lines) {
                Task task = fromString(line);
                if (task instanceof Epic) {
                    super.createEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    super.createSubtask((Subtask) task);
                } else {
                    super.createTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }

    private Task fromString(String line) {
        String[] parts = line.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        String epicIdStr = parts[5];

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(epicIdStr);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private enum TaskType {
        TASK,
        SUBTASK,
        EPIC
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file);
    }
}