package architecture.agent;

import architecture.ClientManager;
import architecture.bdi.ClearBoxDesire;
import architecture.bdi.ClearCellDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Agent;
import board.SokobanObject;
import exceptions.NoAvailableTargetsException;
import logging.ConsoleLogger;
import planning.relaxations.RelaxationType;

import java.util.logging.Logger;

public class PlanningHelper {

    public static final int MAX_CLEARING_DISTANCE = 10;
    private static final Logger LOGGER = ConsoleLogger.getLogger(PlanningHelper.class.getSimpleName());
    private static final int MAX_PLAN_RETRIES = ClientManager.getInstance().getNumberOfAgents() == 1 ? 1 : RelaxationType.values().length - 1;

    private int numFailedPlans;
    private Agent agent;

    public PlanningHelper(Agent agent) {
        this.numFailedPlans = 0;
        this.agent = agent;
    }

    public int getNumFailedPlans() {
        return this.numFailedPlans;
    }

    /**
     * If the plan failed for a GoalDesire, just switch relaxation. If for a ClearXDesire, check if there are more targets available.
     * If not, check if the clearing distance reached the max: if yes, reset it and switch relaxation, else increase it.
     * When the agent will retry to plan with the same desire, it will avoid previously chosen targets.
     *
     * @param desire
     * @param lockDetector
     */
    public void planFailed(Desire desire, LockDetector lockDetector) {
        if (desire instanceof GoalDesire) {
            this.numFailedPlans++;
        } else if (desire instanceof ClearCellDesire || desire instanceof ClearBoxDesire) {
            // Check if all possible clearing targets have been tried with the current relaxation
            SokobanObject blockingObject = desire instanceof ClearCellDesire ? ((ClearCellDesire) desire).getAgent() : desire.getBox();
            try {
                lockDetector.findTargetForObjectToClear(blockingObject, false);
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Plan failed, but I'll try with another target...", agent.getAgentId()));
            } catch (NoAvailableTargetsException e) {
                ConsoleLogger.logInfo(LOGGER, e.getMessage());
                if (lockDetector.getClearingDistance(e.getBlockingObject()) >= MAX_CLEARING_DISTANCE) {
                    lockDetector.restoreClearingDistanceForObject(e.getBlockingObject());
                    this.numFailedPlans++; // Switches to new relaxation
                } else {
                    lockDetector.incrementClearingDistance(e.getBlockingObject());
                }
                lockDetector.clearChosenTargetsForObject(e.getBlockingObject());
            }
        }
    }

    public void noMoreTargets() {
        this.numFailedPlans++;
    }

    public void planSuccessful() {
        this.numFailedPlans = 0;
    }

    public boolean canChangeRelaxation() {
        return this.numFailedPlans <= MAX_PLAN_RETRIES;
    }
}
