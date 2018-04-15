package planning.actions;

import planning.HTNWorldState;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public abstract class CompoundTask implements Task<CompoundTaskType> {

    protected CompoundTaskType taskType;

    /**
     * The current compound task should be refined in a list of 2 or more sub-tasks. It should avoid choosing already blacklisted refinements.
     *
     * @param currentWorldState    Current world state
     * @param refinementsBlacklist set of already blacklisted refinements
     * @param planningStep         current planning step
     * @return a new refinement if any that satisfies the preconditions exists, null otherwise.
     */
    public Refinement findSatisfiedMethod(HTNWorldState currentWorldState, Set<Refinement> refinementsBlacklist, int planningStep) {
        Queue<Refinement> foundRefinements;
        List<Task> subTasks;
        Refinement refinement;
        CompoundTask compoundTask = null;

        // First step: refine task
        switch (taskType) {
            case SolveGoal:
                compoundTask = new SolveGoalTask();
                break;
            case GoToLocation:
                compoundTask = new GoToLocationTask();
                break;
            case MoveBoxToLocation:
                compoundTask = new MoveBoxToLocationTask();
                break;
            default:
                break;
        }
        foundRefinements = compoundTask.refineTask(currentWorldState, refinementsBlacklist, planningStep);

        // Second step: avoid blacklisted refinements (if no valid refinements are found returns null!)
        do {
            refinement = foundRefinements.poll();
        }
        while (!refinementsBlacklist.contains(refinement));
        return refinement;
    }

    protected abstract Queue<Refinement> refineTask(HTNWorldState currentWorldState, Set<Refinement> refinementsBlacklist, int planningStep);

    @Override
    public CompoundTaskType getType() {
        return this.taskType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof CompoundTask))
            return false;
        CompoundTask task = (CompoundTask) other;
        return this.taskType == task.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.taskType);
    }
}
