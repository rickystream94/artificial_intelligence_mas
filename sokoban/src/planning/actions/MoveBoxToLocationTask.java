package planning.actions;

import board.Coordinate;
import planning.HTNWorldState;
import planning.HighLevelPlan;

import java.util.LinkedList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class MoveBoxToLocationTask extends CompoundTask {

    private Coordinate boxDestination;

    public MoveBoxToLocationTask(Coordinate boxDestination) {
        this.boxDestination = boxDestination;
        this.taskType = CompoundTaskType.MoveBoxToLocation;
    }

    @Override
    public Queue<Refinement> refineTask(HTNWorldState currentWorldState, int planningStep) {
        Queue<Refinement> foundRefinements = new PriorityQueue<>(new RefinementComparator(currentWorldState));
        LinkedList<Task> subTasks = new LinkedList<>();

        if (isAchieved(currentWorldState)) {
            foundRefinements.add(new Refinement(this, planningStep));
        } else {
            Direction dirTowardsBox = Direction.getDirection(currentWorldState.getAgentPosition(), currentWorldState.getBoxPosition());

            // Push refinements
            for (Direction dir : Direction.values()) {
                PrimitiveTask pushTask;
                if (!Direction.isOpposite(dir, Objects.requireNonNull(dirTowardsBox))) {
                    pushTask = new PrimitiveTask(PrimitiveTaskType.Push, dirTowardsBox, dir);
                    subTasks.add(pushTask);
                    subTasks.add(this);
                    HighLevelPlan highLevelPlan = new HighLevelPlan(subTasks);
                    foundRefinements.add(new Refinement(this, highLevelPlan, planningStep));
                }
            }

            // Pull refinements
            for (Direction dir : Direction.values()) {
                PrimitiveTask pullTask;
                if (dir != dirTowardsBox) {
                    pullTask = new PrimitiveTask(PrimitiveTaskType.Pull, dir, dirTowardsBox);
                    subTasks.add(pullTask);
                    subTasks.add(this);
                    HighLevelPlan highLevelPlan = new HighLevelPlan(subTasks);
                    foundRefinements.add(new Refinement(this, highLevelPlan, planningStep));
                }
            }
        }
        return foundRefinements;
    }

    @Override
    public boolean isAchieved(HTNWorldState currentWorldState) {
        return currentWorldState.getBoxPosition().equals(boxDestination);
    }
}
