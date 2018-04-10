package planning;

import planning.actions.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A plan made up of a set of actions among which at least one is a HLA
 */
public class HighLevelPlan implements Plan<Task> {

    private Queue<Task> tasks;

    public HighLevelPlan() {
        this.tasks = new LinkedList<>();
    }

    @Override
    public Queue<Task> getTasks() {
        return this.tasks;
    }

    @Override
    public void addTask(Task task) {
        this.tasks.add(task);
    }
}
