package architecture;

import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Agent;
import board.Level;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.*;
import java.util.logging.Logger;

public class DesireHelper {

    private static final Logger LOGGER = ConsoleLogger.getLogger(DesireHelper.class.getSimpleName());

    private Agent agent;
    private Map<Desire, Double> achievedGoalDesiresPriorityMap;
    private Set<Desire> clearPathDesiresAchieved;
    private Desire currentDesire;
    private double currentDesirePriority;
    private Desire lastAchievedDesire;

    public DesireHelper(Agent agent) {
        this.agent = agent;
        this.achievedGoalDesiresPriorityMap = new HashMap<>();
        this.clearPathDesiresAchieved = new HashSet<>();
    }

    /**
     * Get the next desire the agent should commit to. If the agent has just achieved a ClearPathDesire successfully,
     * discard the previous ones that have been enqueued willing to clear the same box.
     *
     * @param desires Heap of all desires
     * @return next desire to process
     */
    public Desire getNextDesire(FibonacciHeap<Desire> desires) {
        FibonacciHeap.Entry<Desire> entry;
        Desire desire;
        int skippedDesires = 0;
        boolean validDesire;
        do {
            entry = desires.dequeueMin();
            final Desire finalDesire = entry.getValue();
            desire = finalDesire;
            if ((desire instanceof ClearPathDesire && lastAchievedDesire instanceof GoalDesire && this.clearPathDesiresAchieved.stream().anyMatch(d -> d.getBox().getObjectId() == finalDesire.getBox().getObjectId() || d.getTarget() == finalDesire.getTarget())) || Level.isDesireAchieved(desire)) {
                skippedDesires++;
                validDesire = false;
            } else validDesire = true;
        }
        while (!validDesire && !desires.isEmpty());
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Skipped %d redundant desires", this.agent.getAgentId(), skippedDesires));
        this.currentDesire = desire;
        this.currentDesirePriority = entry.getPriority();
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
                // Avoid picking the same goal if its priority is the lowest!
                // Penalize the desire (+100)
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Goal desire %s has to be achieved again!", this.agent.getAgentId(), desire));
                desires.enqueue(desire, this.achievedGoalDesiresPriorityMap.get(desire) + 100);
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
        if (currentDesire instanceof GoalDesire) {
            this.achievedGoalDesiresPriorityMap.put(currentDesire, currentDesirePriority);
            this.clearPathDesiresAchieved.forEach(d -> lockDetector.resetClearingDistance(d.getBox()));
            this.clearPathDesiresAchieved.clear();
        } else if (currentDesire instanceof ClearPathDesire) {
            this.clearPathDesiresAchieved.add(currentDesire);
            lockDetector.clearChosenTargets(currentDesire.getBox());
        }
        this.lastAchievedDesire = currentDesire;
    }

    public Desire getCurrentDesire() {
        return currentDesire;
    }

    public double getCurrentDesirePriority() {
        return currentDesirePriority;
    }
}
