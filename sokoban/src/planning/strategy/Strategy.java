package planning.strategy;

import exceptions.NoValidRefinementsException;
import planning.HTNWorldState;
import planning.actions.Refinement;
import planning.actions.Task;

import java.util.*;

public abstract class Strategy {

    private Set<HTNWorldState> exploredStatesSet;
    private Deque<Task> tasksToProcess;
    protected Set<Refinement> refinementsBlacklist;

    public Strategy(Task rootTask) {
        this.exploredStatesSet = new HashSet<>();
        this.tasksToProcess = new ArrayDeque<>();
        this.tasksToProcess.push(rootTask);
        this.refinementsBlacklist = new HashSet<>();
    }

    public boolean hasMoreTasksToProcess() {
        return this.tasksToProcess.isEmpty();
    }

    public Task getNextTaskToProcess() {
        return this.tasksToProcess.pop();
    }

    public void addTaskToProcess(Task task) {
        this.tasksToProcess.push(task);
    }

    public Deque<Task> getTasksToProcess() {
        return this.tasksToProcess;
    }

    public void setTasksToProcess(Deque<Task> tasksToProcess) {
        this.tasksToProcess = tasksToProcess;
    }

    public void addToExploredStates(HTNWorldState worldState) {
        this.exploredStatesSet.add(new HTNWorldState(worldState));
    }

    public boolean isStateExplored(HTNWorldState worldState) {
        return this.exploredStatesSet.contains(worldState);
    }

    public void addRefinementToBlacklist(Refinement refinement) {
        this.refinementsBlacklist.add(refinement);
    }

    public abstract Refinement chooseRefinement(List<Refinement> possibleRefinements) throws NoValidRefinementsException;
}
