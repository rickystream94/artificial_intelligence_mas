package planning.actions;

import planning.HTNWorldState;

import java.util.List;
import java.util.Objects;

public abstract class CompoundTask implements Task<CompoundTaskType> {

    protected CompoundTaskType taskType;

    public abstract List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep);

    public abstract boolean isAchieved(HTNWorldState currentWorldState);

    @Override
    public CompoundTaskType getType() {
        return this.taskType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundTask that = (CompoundTask) o;
        return taskType == that.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType);
    }
}
