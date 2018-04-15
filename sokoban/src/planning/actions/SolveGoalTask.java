package planning.actions;

import planning.HTNWorldState;

import java.util.*;

public class SolveGoalTask extends CompoundTask {

    public SolveGoalTask() {
        this.taskType = CompoundTaskType.SolveGoal;
    }

    @Override
    protected Queue<Refinement> refineTask(HTNWorldState currentWorldState, Set<Refinement> refinementsBlacklist, int planningStep) {
        // Only 1 refinement, no need of PriorityQueue
        Queue<Refinement> foundRefinements = new ArrayDeque<>();
        List<Task> subTasks = new ArrayList<>();

        // If agent hasn't reached the box yet
        if (!currentWorldState.agentCanMoveBox())
            subTasks.add(new GoToLocationTask());
        subTasks.add(new MoveBoxToLocationTask());
        foundRefinements.add(new Refinement(this, subTasks, planningStep));
        return foundRefinements;
    }
}