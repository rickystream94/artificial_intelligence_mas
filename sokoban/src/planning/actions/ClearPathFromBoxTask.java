package planning.actions;

import board.Coordinate;
import planning.HTNWorldState;

import java.util.ArrayList;
import java.util.List;

public class ClearPathFromBoxTask extends CompoundTask {

    public ClearPathFromBoxTask() {
        super.taskType = CompoundTaskType.ClearBox;
    }

    @Override
    public List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep) {
        List<Refinement> foundRefinements = new ArrayList<>();
        if (isAchieved(currentWorldState))
            foundRefinements.add(new Refinement(this, planningStep));
        else
            foundRefinements.add(getSimpleRefinement(currentWorldState, planningStep));
        return foundRefinements;
    }

    @Override
    public boolean isAchieved(HTNWorldState currentWorldState) {
        return currentWorldState.getBoxPosition().equals(currentWorldState.getTarget());
    }

    @Override
    public int calculateApproximation(HTNWorldState worldState) {
        return Coordinate.manhattanDistance(worldState.getAgentPosition(), worldState.getBoxPosition());
    }
}
