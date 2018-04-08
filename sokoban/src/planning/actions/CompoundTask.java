package planning.actions;

import planning.WorldState;

import java.util.Objects;

public class CompoundTask implements Task<CompoundTaskType> {

    private CompoundTaskType taskType;

    public CompoundTask(CompoundTaskType taskType) {
        this.taskType = taskType;
    }

    public Refinement findSatisfiedMethod(WorldState currentWorldState, int planningStep) {
        return null;
        // TODO: to implement --> this is a crucial method, it should probably implement a heuristic check such that the best method is chosen if more preconditions are met (best-first)
    }

    @Override
    public CompoundTaskType getType() {
        return this.taskType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof CompoundTask))
            return false;
        CompoundTask task = (CompoundTask) other;
        return this.taskType == task.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.taskType);
    }
}
