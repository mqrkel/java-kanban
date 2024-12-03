import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.TaskManager;


public class Main {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        TaskManager taskManager = new TaskManager();
        Task task = new Task("Кот", "Покорми кота", TaskStatus.NEW);
        Task task2 = new Task("Магазин", "Сходи в магазин", TaskStatus.NEW);

        Epic epic = new Epic("Путешествие", "Приготовиться к путешествию");
        Epic epic2 = new Epic("Новый год", "Подготовься к новому году");

        Subtask subtask = new Subtask("Документы", 2, "Проверить документы", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Вещи", 2, "Собрать вещи", TaskStatus.NEW);
        Subtask subtask3 = new Subtask("Елка", 3, "Нарядить елку", TaskStatus.NEW);

        taskManager.createTask(task);
        taskManager.createTask(task2);
        taskManager.createEpic(epic);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubtasks());

        System.out.println(taskManager.updateTask(0, new Task("Кот", "Покорми кота", TaskStatus.DONE)));
        System.out.println(taskManager.updateTask(1, new Task("Магазин", "Сходи в магазин", TaskStatus.IN_PROGRESS)));

        System.out.println(taskManager.updateSubtask(4, new Subtask("Документы", 2, "Проверить документы", TaskStatus.DONE)));
        System.out.println(taskManager.updateSubtask(5, new Subtask("Вещи", 2, "Собрать вещи", TaskStatus.DONE)));
        System.out.println(taskManager.updateSubtask(6, new Subtask("Елка", 3, "Нарядить елку", TaskStatus.IN_PROGRESS)));

        System.out.println(taskManager.getEpics());

        taskManager.deleteTask(0);
        taskManager.deleteSubtask(5);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubtasks());
    }
}
