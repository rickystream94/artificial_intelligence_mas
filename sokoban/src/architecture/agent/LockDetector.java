package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import board.Agent;
import board.Box;
import board.Coordinate;
import board.SokobanObject;
import exceptions.NoProgressException;
import exceptions.StuckByForeignBoxException;
import logging.ConsoleLogger;
import planning.actions.Direction;
import planning.actions.PrimitiveTask;
import utils.FibonacciHeap;
import utils.HashMapHelper;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LockDetector {

    private static final Logger LOGGER = ConsoleLogger.getLogger(LockDetector.class.getSimpleName());
    private static final int MAX_PLAN_RETRIES = 1;
    private static final int MAX_ACTION_RETRIES = 1;
    private static final int DEFAULT_CLEARING_DISTANCE = 1;
    private static final int MAX_NO_PROGRESS_COUNTER = 3;

    private Agent agent;
    private LevelManager levelManager;
    private int numFailedPlans;
    private int numFailedActions;
    private int noProgressCounter;
    private Map<Box, Integer> boxClearingDistanceMap;
    private Map<Box, List<Coordinate>> chosenTargetsForClearBox;

    public LockDetector(Agent agent) {
        this.agent = agent;
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.numFailedPlans = 0;
        this.numFailedActions = 0;
        this.noProgressCounter = 0;
        this.boxClearingDistanceMap = new HashMap<>();
        this.chosenTargetsForClearBox = new HashMap<>();
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
    public void detectBlockingObject(PrimitiveTask failedAction, Desire currentDesire, FibonacciHeap<Desire> desires) throws StuckByForeignBoxException, NoProgressException {
        Coordinate blockingCell = getBlockingCellByAction(failedAction, currentDesire);
        SokobanObject blockingObject = this.levelManager.getLevel().dynamicObjectAt(Objects.requireNonNull(blockingCell));

        // Analyze blocking object and decide
        if (blockingObject instanceof Agent) {
            // TODO: the blocking agent's color doesn't matter, in either case it needs to free the cell
        } else if (blockingObject instanceof Box) {
            Box blockingBox = (Box) blockingObject;
            if (blockingBox.getColor() == this.agent.getColor()) {
                // Blocking box is of the same color --> Clear the box
                noProgress();
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Found blocking box %s", this.agent.getAgentId(), blockingBox));
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: %d failed actions since last successful progress", this.agent.getAgentId(), this.noProgressCounter));
                if (needsToReprioritizeDesires())
                    throw new NoProgressException(agent);

                // Create new desire
                Desire clearPathDesire = handleBlockingBox(blockingBox);

                // Avoid priority conflicts: increase all priorities by 1
                List<FibonacciHeap.Entry<Desire>> tempDesires = new ArrayList<>();
                while (!desires.isEmpty())
                    tempDesires.add(desires.dequeueMin());
                tempDesires.forEach(d -> desires.enqueue(d.getValue(), d.getPriority() + 1));

                // Enqueue the new desire with maximum priority
                desires.enqueue(clearPathDesire, -1000);
            } else {
                // Box of different color
                throw new StuckByForeignBoxException(agent, blockingBox);
            }
        }
    }

    public Desire handleBlockingBox(Box blockingBox) throws NoProgressException {
        Coordinate chosenTarget = getTargetForBoxToClear(blockingBox);
        // TODO not very good but it's to avoid NullPointerException when no more targets are available
        if (chosenTarget == null) {
            resetClearingDistance(blockingBox);
            clearChosenTargets(blockingBox);
            throw new NoProgressException(agent);
        }
        return new ClearPathDesire(blockingBox, chosenTarget);
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
     * @return coordinate where the box should be moved
     */
    private Coordinate getTargetForBoxToClear(Box blockingBox) {
        Map<Object, Integer> distances = new HashMap<>();
        int clearingDistance = getClearingDistance(blockingBox);
        Set<Coordinate> edgeCells = getEdgeCells();
        Set<Coordinate> potentialNewPositions = Coordinate.getEmptyCellsWithFixedDistanceFrom(blockingBox.getCoordinate(), clearingDistance);
        //potentialNewPositions.addAll(edgeCells);

        // Prefer edge cells if it's the first attempt to clear this box
        // TODO: this value should be dynamic, depending on the level
        if (clearingDistance < 3 && !edgeCells.isEmpty()) {
            // Target --> Edge cell far away to the blocking box
            edgeCells.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, blockingBox.getCoordinate())));
            return (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);
        }

        // Target --> More distant among the cells with fixed distance from the blocking box
        potentialNewPositions.add(this.agent.getCoordinate());
        //potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, desire.getTarget())));
        potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, blockingBox.getCoordinate())));

        // Avoid choosing the same clearing target if it was already chosen in a previous (failed) iteration
        Coordinate chosenTarget;
        do {
            chosenTarget = (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);
            distances.remove(chosenTarget);
        }
        while (this.chosenTargetsForClearBox.containsKey(blockingBox) && this.chosenTargetsForClearBox.get(blockingBox).contains(chosenTarget));
        if (!this.chosenTargetsForClearBox.containsKey(blockingBox)) {
            List<Coordinate> chosenTargets = new ArrayList<>();
            this.chosenTargetsForClearBox.put(blockingBox, chosenTargets);
        }
        this.chosenTargetsForClearBox.get(blockingBox).add(chosenTarget);

        return chosenTarget;
    }

    public void clearChosenTargets(Box box) {
        this.chosenTargetsForClearBox.remove(box);
    }

    private Set<Coordinate> getEdgeCells() {
        Set<Coordinate> emptyCells = levelManager.getLevel().getEmptyCellsPositions();
        return emptyCells.stream().filter(Coordinate::isEdgeCell).collect(Collectors.toSet());
    }

    public void progressPerformed() {
        this.noProgressCounter = 0;
    }

    private void noProgress() {
        this.noProgressCounter++;
    }

    private boolean needsToReprioritizeDesires() {
        boolean result = this.noProgressCounter == MAX_NO_PROGRESS_COUNTER;
        if (result)
            progressPerformed();
        return result;
    }
}
