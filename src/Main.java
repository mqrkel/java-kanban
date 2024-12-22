import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.TaskManager;

import static service.Managers.getDefault;


public class Main {

    public static void main(String[] args) {
        test();
    }
    private static void test() {
        TaskManager inMemoryTaskManager = getDefault();
        Task task = new Task("Кот", "Покорми кота", TaskStatus.NEW);
        Task task2 = new Task("Магазин", "Сходи в магазин", TaskStatus.NEW);

        Epic epic = new Epic("Путешествие", "Приготовиться к путешествию");
        Epic epic2 = new Epic("Новый год", "Подготовься к новому году");

        Subtask subtask = new Subtask("Документы", epic.getId(), "Проверить документы", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Вещи", epic.getId(), "Собрать вещи", TaskStatus.NEW);
        Subtask subtask3 = new Subtask("Елка", epic2.getId(), "Нарядить елку", TaskStatus.NEW);

        inMemoryTaskManager.createTask(task);
        inMemoryTaskManager.createTask(task2);
        inMemoryTaskManager.createEpic(epic);
        inMemoryTaskManager.createEpic(epic2);
        inMemoryTaskManager.createSubtask(subtask);
        inMemoryTaskManager.createSubtask(subtask2);
        inMemoryTaskManager.createSubtask(subtask3);

        System.out.println(inMemoryTaskManager.getEpics());
        System.out.println(inMemoryTaskManager.getTasks());
        System.out.println(inMemoryTaskManager.getSubtasks());

        System.out.println(inMemoryTaskManager.updateTask(task.getId(), new Task("Кот", "Покорми кота", TaskStatus.DONE)));
        System.out.println(inMemoryTaskManager.updateTask(task2.getId(), new Task("Магазин", "Сходи в магазин", TaskStatus.IN_PROGRESS)));

        System.out.println(inMemoryTaskManager.updateSubtask(subtask.getId(), new Subtask("Документы", epic.getId(), "Проверить документы", TaskStatus.DONE)));
        System.out.println(inMemoryTaskManager.updateSubtask(subtask2.getId(), new Subtask("Вещи", epic.getId(), "Собрать вещи", TaskStatus.DONE)));
        System.out.println(inMemoryTaskManager.updateSubtask(subtask3.getId(), new Subtask("Елка", epic2.getId(), "Нарядить елку", TaskStatus.IN_PROGRESS)));

        System.out.println(inMemoryTaskManager.getEpics());

        System.out.println(inMemoryTaskManager.getTasks());
        System.out.println(inMemoryTaskManager.getSubtasks());
    }
}
