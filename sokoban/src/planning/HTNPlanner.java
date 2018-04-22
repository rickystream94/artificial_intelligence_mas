package planning;

import architecture.bdi.Intention;
import exceptions.NoValidRefinementsException;
import logging.ConsoleLogger;
import planning.actions.*;
import planning.strategy.Strategy;
import planning.strategy.StrategyBestFirst;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

public class HTNPlanner {

    private static final Logger LOGGER = ConsoleLogger.getLogger(HTNPlanner.class.getSimpleName());
    private HTNWorldState currentWorldState;
    private Deque<HTNDecompositionRecord> decompositionHistory;
    private PrimitivePlan finalPlan;
    private int planningStep;
    private Strategy strategy;

    public HTNPlanner(HTNWorldState currentWorldState, Intention intention) {
        this.currentWorldState = currentWorldState;
        this.decompositionHistory = new ArrayDeque<>();
        this.strategy = new StrategyBestFirst(new RefinementComparator(currentWorldState), intention.getTask());
    }

    /**
     * Computes a plan of primitive actions starting from the root compound task and hierarchically refining it
     *
     * @return the final primitive plan
     */
    public PrimitivePlan findPlan() {
        this.finalPlan = new PrimitivePlan();
        this.planningStep = 0;
        this.strategy.addToExploredStates(this.currentWorldState);

        while (!this.strategy.hasMoreTasksToProcess()) { // TODO: OR is purpose of the intention achieved
            Task currentTask = this.strategy.getNextTaskToProcess();
            if (isCompoundTask(currentTask)) {
                // Compound task --> Needs to be refined!
                CompoundTask compoundTask = (CompoundTask) currentTask;
                List<Refinement> foundRefinements = compoundTask.getSatisfiedRefinements(this.currentWorldState, planningStep);
                try {
                    Refinement refinement = this.strategy.chooseRefinement(foundRefinements);
                    recordDecompositionOfTask(refinement);

                    // Sub-tasks are reversed because, in order to maintain the correct order when processing them, the first one added to the stack should be the last one to be processed
                    HighLevelPlan highLevelPlan = refinement.getHighLevelPlan();
                    highLevelPlan.getTasks().descendingIterator().forEachRemaining(subTask -> this.strategy.addTaskToProcess(subTask));
                } catch (NoValidRefinementsException e) {
                    restoreToLastDecomposedTask();
                }
            } else {
                // Primitive task --> Can be added to final plan
                PrimitiveTask primitiveTask = (PrimitiveTask) currentTask;
                Effect effect = primitiveTask.getEffect(currentWorldState.getAgentPosition(), currentWorldState.getBoxPosition());
                this.currentWorldState.applyEffect(effect);
                if (!this.strategy.isStateExplored(this.currentWorldState)) {
                    this.strategy.addToExploredStates(this.currentWorldState);
                    this.finalPlan.addTask(primitiveTask);
                } else
                    restoreToLastDecomposedTask();
            }
            this.planningStep++;

            if (planningStep % 10 == 0)
                ConsoleLogger.logInfo(LOGGER, String.format("Planning step: %d", planningStep));
        }
        ConsoleLogger.logInfo(LOGGER, "Found plan!");
        return this.finalPlan;
    }

    /**
     * Function used to store a snapshot of the current planning iteration, used by the restore function to backtrack. It creates a backup of the refinement, the final plan, the tasks to process and the current world state
     *
     * @param refinement chosen refinement
     */
    private void recordDecompositionOfTask(Refinement refinement) {
        this.decompositionHistory.push(new HTNDecompositionRecord(refinement, new PrimitivePlan(this.finalPlan), ((ArrayDeque<Task>) this.strategy.getTasksToProcess()).clone(), new HTNWorldState(this.currentWorldState)));
    }

    /**
     * Function used to backtrack when a compound task cannot be decomposed or a primitive action leads to an already explored state
     */
    private void restoreToLastDecomposedTask() {
        HTNDecompositionRecord lastSoundPlanningState = this.decompositionHistory.pop();

        // Restore
        this.strategy.setTasksToProcess(lastSoundPlanningState.getTasksToProcess());
        this.finalPlan = lastSoundPlanningState.getFinalPlan();
        this.currentWorldState = lastSoundPlanningState.getWorldState();
        Refinement refinement = lastSoundPlanningState.getRefinement();
        this.strategy.addTaskToProcess(refinement.getOwningCompoundTask());
        this.planningStep = refinement.getPlanningStep() - 1; // Will be increased again at the end of the loop

        // Blacklist refinement (avoid choosing same refinement --> infinite loops)
        this.strategy.addRefinementToBlacklist(refinement);
    }

    private boolean isCompoundTask(Task task) {
        return Arrays.stream(CompoundTaskType.values()).anyMatch(x -> x.equals(task.getType()));
    }
}
