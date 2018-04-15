package planning;

import planning.actions.Task;

import java.util.LinkedList;

/**
 * A plan made up of a set of actions among which at least one is a HLA
 */
public class HighLevelPlan implements Plan<Task> {

    private LinkedList<Task> tasks;

    public HighLevelPlan(LinkedList<Task> tasks) {
        this.tasks = tasks;
    }

    public HighLevelPlan() {
        this.tasks = new LinkedList<>();
    }

    @Override
    public LinkedList<Task> getTasks() {
        return this.tasks;
    }

    @Override
    public void addTask(Task task) {
        this.tasks.add(task);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof HighLevelPlan))
            return false;
        HighLevelPlan hlp = (HighLevelPlan) other;
        return this.tasks.equals(hlp.tasks);
    }
}
