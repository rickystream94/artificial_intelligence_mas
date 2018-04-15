package planning.actions;

import planning.HTNWorldState;

import java.util.Objects;
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
