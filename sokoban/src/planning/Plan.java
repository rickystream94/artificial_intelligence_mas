package planning;

import planning.actions.Task;

import java.util.Queue;

public interface Plan<T extends Task> {
    Queue<? extends T> getTasks();

    void addTask(T task);
}
