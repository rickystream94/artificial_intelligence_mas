package planning.actions;

import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class CompoundTask implements Task<CompoundTaskType> {

    protected CompoundTaskType taskType;

    public abstract List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep);

    public abstract boolean isAchieved(HTNWorldState currentWorldState);

    protected Refinement getSimpleRefinement(HTNWorldState worldState, int planningStep) {
        LinkedList<Task> subTasks = new LinkedList<>();
        subTasks.add(new GoToLocationTask(worldState.getBoxPosition()));
        subTasks.add(new MoveBoxToLocationTask(worldState.getBoxTarget()));
        HighLevelPlan highLevelPlan = new HighLevelPlan(subTasks);
        return new Refinement(this, highLevelPlan, planningStep);
    }

    @Override
    public CompoundTaskType getType() {
        return this.taskType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompoundTask)) return false;
        CompoundTask that = (CompoundTask) o;
        return taskType == that.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType);
    }
}
