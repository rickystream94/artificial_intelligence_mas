package architecture;

import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Agent;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class DesireHelper {

    private static final Logger LOGGER = ConsoleLogger.getLogger(DesireHelper.class.getSimpleName());

    private Agent agent;
    private LevelManager levelManager;
    private Map<Desire, Double> achievedGoalDesiresPriorityMap;
    private Desire previouslyAchievedDesire;
    private Desire currentDesire;
    private double currentDesirePriority;

    public DesireHelper(Agent agent) {
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.agent = agent;
        this.achievedGoalDesiresPriorityMap = new HashMap<>();
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
        int skippedDesires = -1;
        do {
            entry = desires.dequeueMin();
            desire = entry.getValue();
            skippedDesires++;
        }
        while (this.previouslyAchievedDesire instanceof ClearPathDesire && desire instanceof ClearPathDesire && desire.getBox() == this.previouslyAchievedDesire.getBox());
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Skipped %d redundant desires", this.agent.getAgentId(), skippedDesires));
        this.currentDesire = desire;
        this.currentDesirePriority = entry.getPriority();
        this.previouslyAchievedDesire = null;
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
            if (!levelManager.getLevel().isDesireAchieved(desire)) {
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
            if (this.previouslyAchievedDesire instanceof ClearPathDesire)
                lockDetector.resetClearingDistance(this.previouslyAchievedDesire.getBox());
        }
        this.previouslyAchievedDesire = currentDesire;
    }

    public Desire getCurrentDesire() {
        return currentDesire;
    }

    public double getCurrentDesirePriority() {
        return currentDesirePriority;
    }
}
