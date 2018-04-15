package planning.actions;

import planning.HTNWorldState;

import java.util.*;

public class MoveBoxToLocationTask extends CompoundTask {

    public MoveBoxToLocationTask() {
        this.taskType = CompoundTaskType.MoveBoxToLocation;
    }

    @Override
    protected Queue<Refinement> refineTask(HTNWorldState currentWorldState, Set<Refinement> refinementsBlacklist, int planningStep) {
        Queue<Refinement> foundRefinements = new PriorityQueue<>();
        List<Task> subTasks = new ArrayList<>();
        // TODO
        return null;
    }
}
