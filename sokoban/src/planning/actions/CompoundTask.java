package planning.actions;

import planning.HTNWorldState;

import java.util.List;

public abstract class CompoundTask implements Task<CompoundTaskType> {

    protected CompoundTaskType taskType;

    public abstract List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep);

    public abstract boolean isAchieved(HTNWorldState currentWorldState);

    @Override
    public CompoundTaskType getType() {
        return this.taskType;
    }

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();
}
