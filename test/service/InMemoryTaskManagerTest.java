package service;

import org.junit.jupiter.api.BeforeEach;


class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    @BeforeEach
    public void init() {
        taskManager = createTaskManager();
    }


    @Override
    public InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }
}