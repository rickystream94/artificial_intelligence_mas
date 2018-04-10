package planning.actions;

import java.util.List;
import java.util.Objects;

public class Refinement {

    private List<Task> subTasks;
    private CompoundTask owningCompoundTask;
    private int planningStep;

    public Refinement(CompoundTask compoundTask, List<Task> subTasks, int planningStep) {
        this.owningCompoundTask = compoundTask;
        this.subTasks = subTasks;
        this.planningStep = planningStep;
    }

    public List<Task> getSubTasks() {
        return this.subTasks;
    }

    public CompoundTask getOwningCompoundTask() {
        return owningCompoundTask;
    }

    public int getPlanningStep() {
        return planningStep;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof Refinement))
            return false;
        Refinement r = (Refinement) other;
        return this.planningStep == r.planningStep && this.owningCompoundTask == r.owningCompoundTask && this.subTasks.equals(r.subTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.planningStep, this.subTasks, this.owningCompoundTask);
    }
}
