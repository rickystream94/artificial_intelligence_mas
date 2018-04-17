package planning.actions;

import board.Coordinate;
import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class GoToLocationTask extends CompoundTask {

    private Coordinate destination;

    public GoToLocationTask(Coordinate destination) {
        this.destination = destination;
        super.taskType = CompoundTaskType.GoToLocation;
    }

    @Override
    public List<Refinement> getSatisfiedRefinements(HTNWorldState currentWorldState, int planningStep) {
        List<Refinement> foundRefinements = new ArrayList<>();
        LinkedList<Task> subTasks = new LinkedList<>();

        if (isAchieved(currentWorldState)) {
            // Add empty refinement
            foundRefinements.add(new Refinement(this, planningStep));
        } else {
            // Generate all possible moves (optimize by excluding illegal moves)
            for (Direction dir : Direction.values()) {
                PrimitiveTask moveTask = new PrimitiveTask(dir);
                if (!currentWorldState.preconditionsMet(moveTask))
                    continue;
                subTasks.add(moveTask);
                subTasks.add(new GoToLocationTask(destination));
                HighLevelPlan highLevelPlan = new HighLevelPlan(subTasks);
                foundRefinements.add(new Refinement(this, highLevelPlan, planningStep));
            }
        }
        return foundRefinements;
    }

    @Override
    public boolean isAchieved(HTNWorldState currentWorldState) {
        return currentWorldState.getAgentPosition().isNeighbour(this.destination);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof GoToLocationTask))
            return false;
        GoToLocationTask task = (GoToLocationTask) other;
        return this.taskType == task.taskType && this.destination == task.destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.taskType, this.destination);
    }

    @Override
    public int calculateApproximation(HTNWorldState worldState) {
        // TODO: should it include more cost components?
        return Coordinate.manhattanDistance(worldState.getAgentPosition(), this.destination);
    }
}
