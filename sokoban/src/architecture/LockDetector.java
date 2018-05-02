package architecture;

import architecture.bdi.Desire;

import java.util.HashMap;
import java.util.Map;

public class LockDetector {

    private static final int MAX_PLAN_RETRIES = 1;
    private static final int MAX_ACTION_RETRIES = 1;
    private static final int DEFAULT_CLEARING_DISTANCE = 1;

    private int numFailedPlans;
    private int numFailedActions;
    private Map<Desire, Integer> desireClearingDistanceMap;

    public LockDetector() {
        this.numFailedPlans = 0;
        this.numFailedActions = 0;
        this.desireClearingDistanceMap = new HashMap<>();
    }

    public int getNumFailedPlans() {
        return this.numFailedPlans;
    }

    public void planFailed() {
        this.numFailedPlans++;
    }

    public void planSuccessful() {
        this.numFailedPlans = 0;
    }

    public boolean needsReplanning() {
        return this.numFailedPlans == MAX_PLAN_RETRIES;
    }

    public void actionFailed() {
        this.numFailedActions++;
    }

    public void resetFailedActions() {
        this.numFailedActions = 0;
    }

    public boolean isStuck() {
        return this.numFailedActions == MAX_ACTION_RETRIES;
    }

    public int getClearingDistance(Desire desire) {
        if (!this.desireClearingDistanceMap.containsKey(desire)) {
            this.desireClearingDistanceMap.put(desire, DEFAULT_CLEARING_DISTANCE);
            return DEFAULT_CLEARING_DISTANCE;
        }
        // If desire was already stored, increase the clearing distance
        this.desireClearingDistanceMap.put(desire, this.desireClearingDistanceMap.get(desire) + 1);
        return this.desireClearingDistanceMap.get(desire);
    }

    public void resetClearingDistance(Desire desire) {
        this.desireClearingDistanceMap.remove(desire);
    }
}
