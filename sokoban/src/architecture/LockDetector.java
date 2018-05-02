package architecture;

import board.Box;

import java.util.HashMap;
import java.util.Map;

public class LockDetector {

    private static final int MAX_PLAN_RETRIES = 1;
    private static final int MAX_ACTION_RETRIES = 1;
    private static final int DEFAULT_CLEARING_DISTANCE = 1;

    private int numFailedPlans;
    private int numFailedActions;
    private Map<Box, Integer> boxClearingDistanceMap;

    public LockDetector() {
        this.numFailedPlans = 0;
        this.numFailedActions = 0;
        this.boxClearingDistanceMap = new HashMap<>();
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

    public int getClearingDistance(Box box) {
        if (!this.boxClearingDistanceMap.containsKey(box)) {
            this.boxClearingDistanceMap.put(box, DEFAULT_CLEARING_DISTANCE);
            return DEFAULT_CLEARING_DISTANCE;
        }
        // If box was already stored, increase the clearing distance
        this.boxClearingDistanceMap.put(box, this.boxClearingDistanceMap.get(box) + 1);
        return this.boxClearingDistanceMap.get(box);
    }

    public void resetClearingDistance(Box box) {
        this.boxClearingDistanceMap.remove(box);
    }
}
