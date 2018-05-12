package architecture.agent;

import architecture.bdi.ClearBoxDesire;
import architecture.bdi.ClearCellDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Agent;
import board.Level;
import exceptions.NoAvailableTargetsException;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class DesireHelper {

    private static final Logger LOGGER = ConsoleLogger.getLogger(DesireHelper.class.getSimpleName());
    private static final int UNSOLVED_GOAL_PENALTY = 10;
    private static final int BLOCKING_OBJECT_PRIORITY = -1000;

    private Agent agent;
    private Map<Desire, Double> achievedGoalDesiresPriorityMap;
    private Desire currentDesire;
    private double currentDesirePriority;

    public DesireHelper(Agent agent) {
        this.agent = agent;
        this.achievedGoalDesiresPriorityMap = new HashMap<>();
    }

    /**
     * Get the next desire the agent should commit to. If the agent has just achieved a ClearBoxDesire successfully,
     * discard the previous ones that have been enqueued willing to clear the same box.
     *
     * @param desires Heap of all desires
     * @return next desire to process
     * @throws NoAvailableTargetsException If next desire is coming from a blocking object and there are no more targets available (needs to switch relaxation or increase clearing distance)
     */
    public Desire getNextDesire(FibonacciHeap<Desire> desires, LockDetector lockDetector) throws NoAvailableTargetsException {
        FibonacciHeap.Entry<Desire> entry;
        Desire desire;
        int skippedDesires = 0;
        int priority;
        boolean shouldSkipDesire;
        do {
            // Check first if there are any boxes that need to be cleared
            if (lockDetector.hasObjectsToClear()) {
                desire = lockDetector.getDesireFromBlockingObject(lockDetector.getNextBlockingObject());
                priority = BLOCKING_OBJECT_PRIORITY; // By default
                shouldSkipDesire = false;
                continue;
            }
            entry = desires.dequeueMin();
            final Desire finalDesire = entry.getValue();
            priority = (int) entry.getPriority();
            desire = finalDesire;

            // Verify loop-breaking condition --> Is desire already achieved?
            shouldSkipDesire = Level.isDesireAchieved(desire);
            if (shouldSkipDesire)
                skippedDesires++;

            // Check if current desire is GoalDesire and already achieved
            if (desire instanceof GoalDesire && Level.isDesireAchieved(desire)) {
                // We have to back-up the achieved goal desire!
                this.achievedGoalDesiresPriorityMap.put(desire, entry.getPriority());
            }
        } while (shouldSkipDesire && !desires.isEmpty());
        if (skippedDesires > 0)
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Skipped %d already achieved desires", this.agent.getAgentId(), skippedDesires));

        this.currentDesire = desire;
        this.currentDesirePriority = priority;
        return this.currentDesire;
    }

    /**
     * If some previously solved goals are now unsolved, we have to re-enqueue the desire with lower priority
     *
     * @param desires currently remaining desires the agent will commit to
     */
    public void checkAndEnqueueUnsolvedGoalDesires(FibonacciHeap<Desire> desires) {
        Iterator<Desire> it = this.achievedGoalDesiresPriorityMap.keySet().iterator();
        while (it.hasNext()) {
            Desire desire = it.next();
            if (!Level.isDesireAchieved(desire)) {
                // Avoid picking the same desire if its priority is the lowest!
                // Penalize the desire (+100)
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Goal desire %s has to be achieved again!", this.agent.getAgentId(), desire));
                desires.enqueue(desire, this.achievedGoalDesiresPriorityMap.get(desire) + UNSOLVED_GOAL_PENALTY);
                it.remove();
            }
        }
    }

    /**
     * When achieves GoalDesire, back it up and reset clearing distance and targets for blocking objects
     * If ClearX Desire, record progress and remove blocking object.
     */
    public void achievedDesire(LockDetector lockDetector) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: achieved desire %s", this.agent.getAgentId(), currentDesire));
        if (currentDesire instanceof GoalDesire) {
            this.achievedGoalDesiresPriorityMap.put(currentDesire, currentDesirePriority);
            lockDetector.restoreClearingDistancesForAllObjects();
            lockDetector.clearChosenTargetsForAllObjects();
        } else if (currentDesire instanceof ClearBoxDesire || currentDesire instanceof ClearCellDesire) {
            lockDetector.progressPerformed();
            lockDetector.objectCleared();
        }
    }

    public Desire getCurrentDesire() {
        return currentDesire;
    }

    public double getCurrentDesirePriority() {
        return currentDesirePriority;
    }
}
