package planning.actions;

import planning.HTNWorldState;

import java.util.Queue;

public abstract class CompoundTask implements Task<CompoundTaskType> {

    protected CompoundTaskType taskType;

    public abstract Queue<Refinement> refineTask(HTNWorldState currentWorldState, int planningStep);

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
