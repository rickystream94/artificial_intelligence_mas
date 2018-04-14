package planning;

import planning.actions.*;

import java.util.*;

public class HTNPlanner {
    private Deque<Task> tasksToProcess;
    private HTNWorldState currentWorldState;
    private Deque<HTNDecompositionRecord> decompositionHistory;
    private PrimitivePlan finalPlan;
    private int planningStep;
    private Set<Refinement> refinementsBlacklist;

    public HTNPlanner(HTNWorldState currentWorldState, CompoundTask rootTask) {
        this.currentWorldState = currentWorldState;
        this.tasksToProcess = new ArrayDeque<>();
        this.decompositionHistory = new ArrayDeque<>();
        this.refinementsBlacklist = new HashSet<>();
        this.tasksToProcess.push(rootTask);
    }

    /**
     * Computes a plan of primitive actions starting from the root compound task and hierarchically refining it
     *
     * @return the final primitive plan
     */
    public PrimitivePlan findPlan() {
        this.finalPlan = new PrimitivePlan();
        this.planningStep = 0;
        while (!this.tasksToProcess.isEmpty()) {
            Task currentTask = this.tasksToProcess.pop();
            if (Arrays.stream(CompoundTaskType.values()).anyMatch(x -> x.equals(currentTask.getType()))) {
                // Compound task --> Needs to be refined!
                CompoundTask compoundTask = (CompoundTask) currentTask;
                Refinement refinement = compoundTask.findSatisfiedMethod(this.currentWorldState, this.refinementsBlacklist, this.planningStep);
                if (refinement != null) {
                    recordDecompositionOfTask(refinement);

                    // Sub-tasks are reversed because, in order to maintain the correct order when processing them, the first one added to the stack should be the last one to be processed
                    List<Task> subTasks = refinement.getSubTasks();
                    Collections.reverse(subTasks);
                    subTasks.forEach(subTask -> this.tasksToProcess.push(subTask));
                } else
                    restoreToLastDecomposedTask();
            } else {
                // Primitive task --> Can be added to final plan
                PrimitiveTask primitiveTask = (PrimitiveTask) currentTask;
                if (this.currentWorldState.preconditionsMet(primitiveTask)) {
                    Effect effect = primitiveTask.getEffect(currentWorldState.getAgentPosition(), currentWorldState.getBoxPosition());
                    this.currentWorldState.applyEffect(effect);
                    this.finalPlan.addTask(primitiveTask);
                } else
                    restoreToLastDecomposedTask();
            }
            this.planningStep++;
        }
        return this.finalPlan;
    }

    /**
     * Function used to store a snapshot of the current planning iteration, used by the restore function to backtrack. It creates a backup of the refinement, the final plan, the tasks to process and the current world state
     *
     * @param refinement chosen refinement
     */
    private void recordDecompositionOfTask(Refinement refinement) {
        this.decompositionHistory.push(new HTNDecompositionRecord(refinement, new PrimitivePlan(this.finalPlan), ((ArrayDeque<Task>) this.tasksToProcess).clone(), new HTNWorldState(this.currentWorldState)));
    }

    /**
     * Function used to backtrack when a compound task cannot be decomposed or a primitive action's preconditions are not met
     */
    private void restoreToLastDecomposedTask() {
        HTNDecompositionRecord lastSoundPlanningState = this.decompositionHistory.pop();

        // Restore
        this.tasksToProcess = lastSoundPlanningState.getTasksToProcess();
        this.finalPlan = lastSoundPlanningState.getFinalPlan();
        this.currentWorldState = lastSoundPlanningState.getWorldState();
        Refinement refinement = lastSoundPlanningState.getRefinement();
        this.tasksToProcess.push(refinement.getOwningCompoundTask());
        this.planningStep = refinement.getPlanningStep() - 1; // Will be increased again at the end of the loop

        // Blacklist refinement (avoid choosing same refinement --> infinite loops)
        this.refinementsBlacklist.add(refinement);
    }
}
