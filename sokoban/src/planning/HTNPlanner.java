package planning;

import architecture.bdi.Desire;
import exceptions.NoValidRefinementsException;
import exceptions.PlanNotFoundException;
import logging.ConsoleLogger;
import planning.actions.*;
import planning.strategy.Strategy;
import planning.strategy.StrategyBestFirst;
import utils.Memory;

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
    private int planFailureCounter;

    public HTNPlanner(HTNWorldState currentWorldState, Desire desire) {
        this.currentWorldState = currentWorldState;
        this.decompositionHistory = new ArrayDeque<>();
        this.strategy = new StrategyBestFirst(new RefinementComparator(currentWorldState), desire);
    }

    /**
     * Computes a plan of primitive actions starting from the root compound task and hierarchically refining it
     *
     * @return the final primitive plan
     */
    public PrimitivePlan findPlan() throws PlanNotFoundException {
        this.finalPlan = new PrimitivePlan();
        this.planningStep = 0;
        this.planFailureCounter = 0;
        this.strategy.addToExploredStates(this.currentWorldState);

        while (!this.strategy.hasMoreTasksToProcess()) {
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

            // Keep track of memory usage
            if (planningStep % 500 == 0 && planningStep != 0) {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Planning step: %d", this.currentWorldState.getAgentId(), planningStep));
                ConsoleLogger.logInfo(LOGGER, Memory.stringRep());
            }

            // Check if planning is taking too long
            if (planningStep >= 10000)
                throw new PlanNotFoundException(this.currentWorldState.getAgentId());
        }

        if (this.finalPlan.getTasks().isEmpty())
            throw new PlanNotFoundException(this.currentWorldState.getAgentId());

        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Found plan!", this.currentWorldState.getAgentId()));
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
    private void restoreToLastDecomposedTask() throws PlanNotFoundException {
        if (!this.decompositionHistory.isEmpty()) {
            HTNDecompositionRecord lastSoundPlanningState = this.decompositionHistory.pop();

            // Restore
            this.strategy.setTasksToProcess(lastSoundPlanningState.getTasksToProcess());
            this.finalPlan = lastSoundPlanningState.getFinalPlan();
            this.currentWorldState = lastSoundPlanningState.getWorldState();
            Refinement refinement = lastSoundPlanningState.getRefinement();
            this.strategy.addTaskToProcess(refinement.getOwningCompoundTask());
            this.strategy.updateStatus(this.currentWorldState);
            this.planningStep = refinement.getPlanningStep() - 1; // Will be increased again at the end of the loop

            // Blacklist refinement (avoid choosing same refinement --> infinite loops)
            this.strategy.addRefinementToBlacklist(refinement);
        }

        // Check if we brought planningStep back to 0 --> No plan can be found --> Throw
        if (this.planningStep >= -1 && this.planningStep <= 1) {
            this.planFailureCounter++;
            if (this.planFailureCounter > 50) // Threshold high enough
                throw new PlanNotFoundException(this.currentWorldState.getAgentId());
        } else
            this.planFailureCounter = 0;
    }

    private boolean isCompoundTask(Task task) {
        return Arrays.stream(CompoundTaskType.values()).anyMatch(x -> x.equals(task.getType()));
    }
}
