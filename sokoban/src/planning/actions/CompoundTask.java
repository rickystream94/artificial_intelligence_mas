package planning.actions;

import planning.HTNWorldState;

import java.util.Objects;
import java.util.Set;

public class CompoundTask implements Task<CompoundTaskType> {

    private CompoundTaskType taskType;

    public CompoundTask(CompoundTaskType taskType) {
        this.taskType = taskType;
    }

    /**
     * The current compound task should be refined in a list of 2 or more sub-tasks. It should avoid choosing already blacklisted refinements.
     *
     * @param currentWorldState    Current world state
     * @param refinementsBlacklist set of already blacklisted refinements
     * @param planningStep         current planning step
     * @return a new refinement if any that satisfies the preconditions exists, null otherwise.
     */
    public Refinement findSatisfiedMethod(HTNWorldState currentWorldState, Set<Refinement> refinementsBlacklist, int planningStep) {
        /*do {
            Refinement refinement = ...
        }
        while (!refinementsBlacklist.contains(refinement));
        return refinement;*/
        // TODO: to implement --> this is a crucial method, it should probably implement a heuristic check such that the best method is chosen if more preconditions are met (best-first)
        return null;
    }

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
