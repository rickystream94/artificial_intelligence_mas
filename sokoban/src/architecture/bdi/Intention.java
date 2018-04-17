package architecture.bdi;

import planning.actions.Task;

public class Intention {
    private Task task;

    public Intention(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
