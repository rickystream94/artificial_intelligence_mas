package planning;

import planning.actions.Refinement;
import planning.actions.Task;

import java.util.Deque;

public class HTNDecompositionRecord {

    private Refinement refinement;
    private PrimitivePlan finalPlan;
    private Deque<Task> tasksToProcess;
    private WorldState worldState;

    public HTNDecompositionRecord(Refinement refinement, PrimitivePlan finalPlan, Deque<Task> tasksToProcess, WorldState worldState) {
        this.refinement = refinement;
        this.finalPlan = finalPlan;
        this.tasksToProcess = tasksToProcess;
        this.worldState = worldState;
    }

    public Refinement getRefinement() {
        return refinement;
    }

    public PrimitivePlan getFinalPlan() {
        return finalPlan;
    }

    public Deque<Task> getTasksToProcess() {
        return tasksToProcess;
    }

    public WorldState getWorldState() {
        return worldState;
    }
}
