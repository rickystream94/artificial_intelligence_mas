package planning.actions;

import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class SolveGoalTask extends CompoundTask {

    public SolveGoalTask() {
        this.taskType = CompoundTaskType.SolveGoal;
    }

    @Override
    public Queue<Refinement> refineTask(HTNWorldState currentWorldState, int planningStep) {
        // Only 1 refinement, no need of PriorityQueue
        Queue<Refinement> foundRefinements = new ArrayDeque<>();
        LinkedList<Task> subTasks = new LinkedList<>();

        if (isAchieved(currentWorldState)) {
            foundRefinements.add(new Refinement(this, planningStep));
        } else {
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
}
