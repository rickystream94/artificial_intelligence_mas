package planning;

import planning.actions.Task;

import java.util.LinkedList;
import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(tasks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HighLevelPlan that = (HighLevelPlan) o;
        return Objects.equals(tasks, that.tasks);
    }
}
