package architecture;

import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import board.Agent;
import board.Box;
import board.Coordinate;
import board.SokobanObject;
import exceptions.StuckByForeignBoxException;
import logging.ConsoleLogger;
import planning.actions.Direction;
import planning.actions.PrimitiveTask;
import utils.FibonacciHeap;
import utils.HashMapHelper;

import java.util.*;
import java.util.logging.Logger;

public class LockDetector {

    private static final Logger LOGGER = ConsoleLogger.getLogger(LockDetector.class.getSimpleName());
    private static final int MAX_PLAN_RETRIES = 1;
    private static final int MAX_ACTION_RETRIES = 1;
    private static final int DEFAULT_CLEARING_DISTANCE = 1;

    private Agent agent;
    private LevelManager levelManager;
    private int numFailedPlans;
    private int numFailedActions;
    private Map<Box, Integer> boxClearingDistanceMap;

    public LockDetector(Agent agent) {
        this.agent = agent;
        this.levelManager = ClientManager.getInstance().getLevelManager();
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

    private int getClearingDistance(Box box) {
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

    /**
     * This method is in charge of letting the agent figure out whether he can solve the blocking issue on its own or
     * he needs to ask for help
     *
     * @param failedAction  last action that failed
     * @param currentDesire current desire to achieve
     */
    public void detectBlockingObject(PrimitiveTask failedAction, Desire currentDesire, FibonacciHeap<Desire> desires) throws StuckByForeignBoxException {
        Coordinate blockingCell = getBlockingCellByAction(failedAction, currentDesire);
        SokobanObject blockingObject = this.levelManager.getLevel().dynamicObjectAt(Objects.requireNonNull(blockingCell));

        // Examine blocking object and decide
        if (blockingObject instanceof Agent) {
            // TODO: the blocking agent's color doesn't matter, in either case it needs to free the cell
        } else if (blockingObject instanceof Box) {
            Box blockingBox = (Box) blockingObject;
            if (blockingBox.getColor() == this.agent.getColor()) {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Found blocking box %s", this.agent.getAgentId(), blockingBox));
                // Blocking box is of the same color --> Clear the box
                Coordinate chosenTarget = getTargetForBoxToClear(blockingBox, currentDesire);

                // Avoid priority conflicts: increase all priorities by 1
                List<FibonacciHeap.Entry<Desire>> tempDesires = new ArrayList<>();
                while (!desires.isEmpty())
                    tempDesires.add(desires.dequeueMin());
                tempDesires.forEach(d -> desires.enqueue(d.getValue(), d.getPriority() + 1));

                // Create new desire and enqueue it with maximum priority
                ClearPathDesire clearPathDesire = new ClearPathDesire(blockingBox, chosenTarget);
                desires.enqueue(clearPathDesire, -1000);
            } else {
                // Box of different color
                throw new StuckByForeignBoxException(agent, blockingBox);
            }
        }
    }

    /**
     * Given the failed action, returns the cell that prevents it to be performed successfully
     *
     * @param task
     * @param desire
     * @return
     */
    private Coordinate getBlockingCellByAction(PrimitiveTask task, Desire desire) {
        Coordinate blockingCell;
        switch (task.getType()) {
            case Move:
                blockingCell = Direction.getPositionByDirection(this.agent.getCoordinate(), task.getDir1());
                break;
            case Push:
                Box box = desire.getBox();
                blockingCell = Direction.getPositionByDirection(box.getCoordinate(), task.getDir2());
                break;
            case Pull:
                blockingCell = Direction.getPositionByDirection(this.agent.getCoordinate(), task.getDir1());
                break;
            default:
                blockingCell = null; // Unreachable
        }
        return blockingCell;
    }

    /**
     * Finds the cell where the blocking box should be moved
     *
     * @param blockingBox blocking box
     * @param desire      current desire
     * @return coordinate where the box should be moved
     */
    private Coordinate getTargetForBoxToClear(Box blockingBox, Desire desire) {
        int clearingDistance = getClearingDistance(blockingBox);
        List<Coordinate> potentialNewPositions = Coordinate.getEmptyCellsWithFixedDistanceFrom(blockingBox.getCoordinate(), clearingDistance);
        // TODO: there should be a more well-structured heuristic --> the most preferred cells are the edge ones
        // surrounded only by walls/boxes and distant from the goals (e.g. SAtowersOfSaigon5)
        potentialNewPositions.add(this.agent.getCoordinate());
        Map<Object, Integer> distances = new HashMap<>();
        potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, desire.getTarget())));

        return (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);
    }
}
