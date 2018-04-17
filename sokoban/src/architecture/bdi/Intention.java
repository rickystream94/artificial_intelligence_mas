package architecture.bdi;

import planning.actions.Task;

import java.util.Objects;

public class Intention {
    private Task task;

    public Intention(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Intention intention = (Intention) o;
        return Objects.equals(task, intention.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task);
    }
}
