package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.*;
import board.Agent;
import exceptions.NoAvailableBoxesException;
import exceptions.NoAvailableGoalsException;
import exceptions.NoAvailableTargetsException;
import exceptions.NotLowestPriorityGoalException;
import logging.ConsoleLogger;

import java.util.logging.Logger;

public class DesireHelper {

    private static final Logger LOGGER = ConsoleLogger.getLogger(DesireHelper.class.getSimpleName());

    private Agent agent;
    private Desire currentDesire;
    private LevelManager levelManager;
    private BDIManager bdiManager;

    public DesireHelper(Agent agent) {
        this.agent = agent;
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.bdiManager = ClientManager.getInstance().getBdiManager();
    }

    /**
     * Get the next desire the agent should commit to. If the agent has just achieved a ClearBoxDesire successfully,
     * discard the previous ones that have been enqueued willing to clear the same box.
     *
     * @return next desire to process
     * @throws NoAvailableTargetsException If next desire is coming from a blocking object and there are no more targets available (needs to switch relaxation or increase clearing distance)
     */
    public Desire getNextDesire(LockDetector lockDetector) throws NoAvailableTargetsException, NoAvailableBoxesException, NotLowestPriorityGoalException, NoAvailableGoalsException {
        Desire desire;
        int skippedDesires = 0;
        boolean shouldSkipDesire;
        this.bdiManager.checkIfSolvedGoalsAreStillSolved();
        do {
            // Check first if there are any boxes that need to be cleared
            if (lockDetector.hasObjectsToClear()) {
                desire = lockDetector.getDesireFromBlockingObject(lockDetector.getNextBlockingObject());
                shouldSkipDesire = false;
                continue;
            }
            desire = this.bdiManager.getNextGoalDesireForAgent(this.agent);
            assert desire != null;

            // Verify loop-breaking condition --> Is desire already achieved?
            shouldSkipDesire = this.levelManager.getLevel().isDesireAchieved(desire);
            if (shouldSkipDesire)
                skippedDesires++;

            // Check if current desire is GoalDesire and already achieved
            if (desire instanceof GoalDesire && shouldSkipDesire)
                this.bdiManager.solvedGoal(agent, desire);
        } while (shouldSkipDesire);
        if (skippedDesires > 0)
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Skipped %d already achieved desires", this.agent.getAgentId(), skippedDesires));

        this.currentDesire = desire;
        this.bdiManager.agentCommitsToDesire(agent, desire);
        return this.currentDesire;
    }

    /**
     * When achieves GoalDesire, back it up and reset clearing distance and targets for blocking objects
     * If ClearX Desire, record progress and remove blocking object.
     */
    public void achievedDesire(LockDetector lockDetector) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: achieved desire %s", this.agent.getAgentId(), currentDesire));
        if (currentDesire instanceof GoalDesire) {
            this.bdiManager.solvedGoal(agent, currentDesire);
            lockDetector.restoreClearingDistancesForAllObjects();
            lockDetector.clearChosenTargetsForAllObjects();
            lockDetector.clearFalsePositiveBlockingObjects();
        } else if (currentDesire instanceof ClearBoxDesire || currentDesire instanceof ClearCellDesire) {
            lockDetector.progressPerformed();
            lockDetector.objectCleared();
            this.bdiManager.resetAgentDesire(agent);
        }
    }

    public Desire getCurrentDesire() {
        return currentDesire;
    }
}
