package architecture.agent;

import architecture.bdi.ClearBoxDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Agent;
import board.Level;
import exceptions.NoProgressException;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class DesireHelper {

    private static final Logger LOGGER = ConsoleLogger.getLogger(DesireHelper.class.getSimpleName());

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
     */
    public Desire getNextDesire(FibonacciHeap<Desire> desires, LockDetector lockDetector) throws NoProgressException {
        FibonacciHeap.Entry<Desire> entry;
        Desire desire;
        int skippedDesires = 0;
        int priority;
        boolean shouldSkipDesire;
        do {
            // Check first if there are any boxes that need to be cleared
            if (lockDetector.hasObjectsToClear()) {
                desire = lockDetector.handleBlockingObject(lockDetector.getNextBlockingObject());
                priority = -1000; // By default
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
                //lockDetector.restoreClearingDistancesForAllObjects();
            }
        } while (shouldSkipDesire && !desires.isEmpty());
        if (skippedDesires > 0)
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Skipped %d redundant desires", this.agent.getAgentId(), skippedDesires));

        // Check if desire is valid (there is progress)
        if (desire.getTarget() == null)
            throw new NoProgressException(this.agent);

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
                // TODO: this punishment is currently hardcoded
                desires.enqueue(desire, this.achievedGoalDesiresPriorityMap.get(desire) + 10);
                it.remove();
            }
        }
    }

    /**
     * Backup achieved goal desires and reset clearing distance for previously achieved clear path desires
     * Indeed, if we have now achieved a goal desire after the previous clear path desire was achieved as well,
     * the clearing distance may be restored to default.
     */
    public void achievedDesire(LockDetector lockDetector) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: achieved desire %s", this.agent.getAgentId(), currentDesire));
        if (currentDesire instanceof GoalDesire) {
            this.achievedGoalDesiresPriorityMap.put(currentDesire, currentDesirePriority);
            lockDetector.restoreClearingDistancesForAllObjects();
            lockDetector.clearChosenTargetsForAllObjects();
        } else if (currentDesire instanceof ClearBoxDesire) {
            lockDetector.clearChosenTargetsForObject(currentDesire.getBox());
            //lockDetector.resetClearingDistanceForBox(currentDesire.getBox());
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
