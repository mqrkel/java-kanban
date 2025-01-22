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
        testSprint6();
    }

    private static void testSprint6() {
        TaskManager inMemoryTaskManager = getDefault();
        Task task1 = inMemoryTaskManager.createTask(new Task("Кот", "Покорми кота"));
        Task task2 = inMemoryTaskManager.createTask(new Task("Магазин", "Сходи в магазин"));

        Epic epic1 = inMemoryTaskManager.createEpic(new Epic("Путешествие", "Приготовиться к путешествию"));
        Epic epic2 = inMemoryTaskManager.createEpic(new Epic("Новый год", "Подготовься к новому году"));

        Subtask subtask1 = inMemoryTaskManager.createSubtask(new Subtask("Документы", "Проверить документы", epic1.getId()));
        Subtask subtask2 = inMemoryTaskManager.createSubtask(new Subtask("Вещи", "Собрать вещи", epic1.getId()));
        Subtask subtask3 = inMemoryTaskManager.createSubtask(new Subtask("Кот", "Отдать кота", epic1.getId()));

        inMemoryTaskManager.getTaskById(task1.getId());
        System.out.println(inMemoryTaskManager.getAllTasksInHistoryList() + System.lineSeparator());
        inMemoryTaskManager.getTaskById(task1.getId());
        System.out.println(inMemoryTaskManager.getAllTasksInHistoryList() + System.lineSeparator());

        inMemoryTaskManager.getEpicById(epic1.getId());
        inMemoryTaskManager.getSubtaskById(subtask1.getId());
        inMemoryTaskManager.getSubtaskById(subtask2.getId());
        inMemoryTaskManager.getSubtaskById(subtask3.getId());
        System.out.println(inMemoryTaskManager.getAllTasksInHistoryList()+System.lineSeparator());

        inMemoryTaskManager.deleteEpic(epic1.getId());
        System.out.println(inMemoryTaskManager.getAllTasksInHistoryList()+System.lineSeparator());
    }
}
