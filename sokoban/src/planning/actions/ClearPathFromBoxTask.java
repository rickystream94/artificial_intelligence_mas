package planning.actions;

import board.Coordinate;
import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ClearPathFromBoxTask extends CompoundTask {

    public ClearPathFromBoxTask() {
        super.taskType = CompoundTaskType.ClearBox;
    }

    @Override
    public List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep) {
        List<Refinement> foundRefinements = new ArrayList<>();
        LinkedList<Task> subTasks = new LinkedList<>();
        subTasks.add(new GoToLocationTask(currentWorldState.getBoxPosition()));
        subTasks.add(new MoveBoxToLocationTask(currentWorldState.getBoxTarget()));
        HighLevelPlan highLevelPlan = new HighLevelPlan(subTasks);
        foundRefinements.add(new Refinement(this, highLevelPlan, planningStep));
        return foundRefinements;
    }

    @Override
    public boolean isAchieved(HTNWorldState currentWorldState) {
        // This method is not used for this task because there will not be recursion
        // There is no way to check it's achieved other than planning
        return false;
    }

    @Override
    public int calculateApproximation(HTNWorldState worldState) {
        return Coordinate.manhattanDistance(worldState.getAgentPosition(), worldState.getBoxPosition());
    }
}
