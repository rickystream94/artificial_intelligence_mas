package planning.actions;

import board.Coordinate;
import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class GoToLocationTask extends CompoundTask {

    private Coordinate destination;

    public GoToLocationTask(Coordinate destination) {
        this.destination = destination;
        super.taskType = CompoundTaskType.GoToLocation;
    }

    @Override
    public Queue<Refinement> refineTask(HTNWorldState currentWorldState, int planningStep) {
        Queue<Refinement> foundRefinements = new PriorityQueue<>(new RefinementComparator(currentWorldState));
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
}
