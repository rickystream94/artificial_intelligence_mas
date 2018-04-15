package planning.actions;

import planning.HTNWorldState;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MoveBoxToLocationTask extends CompoundTask {

    public MoveBoxToLocationTask() {
        this.taskType = CompoundTaskType.MoveBoxToLocation;
    }

    @Override
    protected Queue<Refinement> refineTask(HTNWorldState currentWorldState, int planningStep) {
        Queue<Refinement> foundRefinements = new PriorityQueue<>();
        List<Task> subTasks = new ArrayList<>();
        // TODO
        return null;
    }
}
