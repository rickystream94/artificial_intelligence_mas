package planning.actions;

import planning.HTNWorldState;

import java.util.*;

public class GoToLocationTask extends CompoundTask {

    public GoToLocationTask() {
        super.taskType = CompoundTaskType.GoToLocation;
    }

    @Override
    protected Queue<Refinement> refineTask(HTNWorldState currentWorldState, Set<Refinement> refinementsBlacklist, int planningStep) {
        Queue<Refinement> foundRefinements = new PriorityQueue<>();
        List<Task> subTasks = new ArrayList<>();

        // If agent hasn't reached the box yet
        if (!currentWorldState.agentCanMoveBox()) {
            // Check if agent can get closer to the box
            // TODO

            subTasks.add(new GoToLocationTask());
        }

        return foundRefinements;
    }
}