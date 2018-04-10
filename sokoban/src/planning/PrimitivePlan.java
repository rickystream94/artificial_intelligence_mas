package planning;

import planning.actions.PrimitiveTask;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents a plan made only by primitive tasks that can't be refined anymore
 */
public class PrimitivePlan implements Plan<PrimitiveTask> {

    private Queue<PrimitiveTask> tasks;

    public PrimitivePlan() {
        this.tasks = new LinkedList<>();
    }

    /**
     * Copy constructor
     * @param other
     */
    public PrimitivePlan(PrimitivePlan other) {
        this.tasks = other.tasks;
    }

    @Override
    public Queue<PrimitiveTask> getTasks() {
        return this.tasks;
    }

    @Override
    public void addTask(PrimitiveTask task) {
        this.tasks.add(task);
    }
}
