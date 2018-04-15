package planning.actions;

import planning.HighLevelPlan;

import java.util.Objects;

public class Refinement {

    private HighLevelPlan subTasks;
    private CompoundTask owningCompoundTask;
    private int planningStep;

    public Refinement(CompoundTask compoundTask, HighLevelPlan subTasks, int planningStep) {
        this.owningCompoundTask = compoundTask;
        this.subTasks = subTasks;
        this.planningStep = planningStep;
    }

    public Refinement(CompoundTask compoundTask, int planningStep) {
        this.owningCompoundTask = compoundTask;
        this.subTasks = new HighLevelPlan();
        this.planningStep = planningStep;
    }

    public HighLevelPlan getHighLevelPlan() {
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
