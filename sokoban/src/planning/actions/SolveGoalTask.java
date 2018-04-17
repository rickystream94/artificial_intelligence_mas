package planning.actions;

import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SolveGoalTask extends CompoundTask {

    public SolveGoalTask() {
        this.taskType = CompoundTaskType.SolveGoal;
    }

    @Override
    public List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep) {
        List<Refinement> foundRefinements = new ArrayList<>();

        if (isAchieved(currentWorldState)) {
            foundRefinements.add(new Refinement(this, planningStep));
        } else {
            LinkedList<Task> subTasks = new LinkedList<>();
            subTasks.add(new GoToLocationTask(currentWorldState.getBoxPosition()));
            subTasks.add(new MoveBoxToLocationTask(currentWorldState.getGoalPosition()));
            HighLevelPlan highLevelPlan = new HighLevelPlan(subTasks);
            foundRefinements.add(new Refinement(this, highLevelPlan, planningStep));
        }
        return foundRefinements;
    }

    @Override
    public boolean isAchieved(HTNWorldState currentWorldState) {
        return currentWorldState.getBoxPosition().equals(currentWorldState.getGoalPosition());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!super.equals(other)) return false;
        if (!(other instanceof SolveGoalTask)) return false;
        SolveGoalTask task = (SolveGoalTask) other;
        return this.taskType == task.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.taskType);
    }

    @Override
    public int calculateApproximation(HTNWorldState worldState) {
        // TODO: since this is the root compound task that, in planning, would exist only once, the implementation of this method
        // should provide a heuristic cost for prioritizing DESIRES
        return 0;
    }
}
