package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.ClearBoxDesire;
import architecture.bdi.ClearCellDesire;
import architecture.bdi.Desire;
import board.*;
import exceptions.NoProgressException;
import exceptions.StuckByAgentException;
import exceptions.StuckByForeignBoxException;
import logging.ConsoleLogger;
import planning.actions.Direction;
import planning.actions.PrimitiveTask;
import utils.HashMapHelper;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LockDetector {

    private static final Logger LOGGER = ConsoleLogger.getLogger(LockDetector.class.getSimpleName());
    private static final int MAX_PLAN_RETRIES = 2;
    private static final int MAX_ACTION_RETRIES = ClientManager.getInstance().getNumberOfAgents() == 1 ? 1 : 3;
    private static final int DEFAULT_CLEARING_DISTANCE = 1;
    private static final int MAX_NO_PROGRESS_COUNTER = 3;

    private Agent agent;
    private LevelManager levelManager;
    private int numFailedPlans;
    private int numFailedActions;
    private int noProgressCounter;
    private Map<SokobanObject, Integer> objectClearingDistanceMap;
    private Map<SokobanObject, Set<Coordinate>> chosenTargetsForObjectToClear;
    private Deque<SokobanObject> blockingObjects;

    public LockDetector(Agent agent) {
        this.agent = agent;
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.numFailedPlans = 0;
        this.numFailedActions = 0;
        this.noProgressCounter = 0;
        this.objectClearingDistanceMap = new HashMap<>();
        this.chosenTargetsForObjectToClear = new HashMap<>();
        this.blockingObjects = new ArrayDeque<>();
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

    public boolean shouldChangeRelaxation() {
        return this.numFailedPlans <= MAX_PLAN_RETRIES;
    }

    public void actionFailed() {
        this.numFailedActions++;
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: action failed, attempt %d/%d", agent.getAgentId(), numFailedActions, MAX_ACTION_RETRIES));
    }

    public void resetFailedActions() {
        this.numFailedActions = 0;
    }

    public boolean isStuck() {
        return this.numFailedActions == MAX_ACTION_RETRIES;
    }

    private int getClearingDistance(SokobanObject object) {
        if (!this.objectClearingDistanceMap.containsKey(object)) {
            this.objectClearingDistanceMap.put(object, DEFAULT_CLEARING_DISTANCE);
            return DEFAULT_CLEARING_DISTANCE;
        }
        // If object was already stored, increase the clearing distance
        this.objectClearingDistanceMap.put(object, this.objectClearingDistanceMap.get(object) + 1);
        return this.objectClearingDistanceMap.get(object);
    }

    public void restoreClearingDistancesForAllObjects() {
        this.objectClearingDistanceMap.clear();
    }

    /**
     * This method is in charge of letting the agent figure out whether he can solve the blocking issue on its own or
     * he needs to ask for help
     *
     * @param failedAction  last action that failed
     * @param currentDesire current desire to achieve
     */
    public void detectBlockingObject(PrimitiveTask failedAction, Desire currentDesire) throws StuckByForeignBoxException, NoProgressException, StuckByAgentException {
        Coordinate blockingCell = getBlockingCellByAction(failedAction, currentDesire);
        SokobanObject blockingObject = this.levelManager.getLevel().dynamicObjectAt(Objects.requireNonNull(blockingCell));

        // Analyze blocking object and decide
        if (blockingObject instanceof Agent) {
            Agent blockingAgent = (Agent) blockingObject;
            if (blockingAgent.getStatus() == AgentStatus.STUCK) {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: False alarm, found stuck agent %c. I'll recalculate my target...", this.agent.getAgentId(), blockingAgent.getAgentId()));
                return;
            }

            // Ask for help
            throw new StuckByAgentException(agent, blockingAgent);
        } else if (blockingObject instanceof Box) {
            Box blockingBox = (Box) blockingObject;
            if (blockingBox.getColor() == this.agent.getColor()) {
                // Blocking box is of the same color --> Clear the box
                noProgress();
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Found blocking box %s", this.agent.getAgentId(), blockingBox));
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: %d failed actions since last successful progress", this.agent.getAgentId(), this.noProgressCounter));
                if (needsToReprioritizeDesires())
                    throw new NoProgressException(agent);

                // Record the blocking box
                addBlockingObject(blockingBox);
            } else {
                // Box of different color --> Ask for help
                throw new StuckByForeignBoxException(agent, blockingBox);
            }
        }
    }

    public Desire handleBlockingObject(SokobanObject blockingObject) {
        Coordinate chosenTarget = getTargetForObjectToClear(blockingObject);

        if (blockingObject instanceof Box)
            return new ClearBoxDesire((Box) blockingObject, chosenTarget);
        else if (blockingObject instanceof Agent)
            return new ClearCellDesire((Agent) blockingObject, chosenTarget);
        // TODO the agent might not be able to free the cell without moving a box! Check the neighbours

        return null;
    }

    public void objectCleared() {
        if (!this.blockingObjects.isEmpty())
            this.blockingObjects.pop();
    }

    public boolean hasObjectsToClear() {
        return !this.blockingObjects.isEmpty();
    }

    public SokobanObject getNextBlockingObject() {
        return this.blockingObjects.peek();
    }

    public void clearBlockingObjects() {
        this.blockingObjects.clear();
    }

    public void addBlockingObject(SokobanObject blockingObject) {
        // TODO: somehow hardcoded, but works, possibility of improvements
        //if (this.blockingObjects.stream().noneMatch(object -> object == blockingObject))
        if (this.blockingObjects.stream().filter(object -> object == blockingObject).count() <= 3)
            this.blockingObjects.push(blockingObject);
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
     * @param blockingObject blocking object (box or agent)
     * @return coordinate where the object should be moved
     */
    private Coordinate getTargetForObjectToClear(SokobanObject blockingObject) {
        Map<Object, Integer> distances = new HashMap<>();
        int clearingDistance = getClearingDistance(blockingObject);
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: clearing distance for %s is %d.", this.agent.getAgentId(), blockingObject, clearingDistance));
        Set<Coordinate> edgeCells = getEdgeCells();
        Set<Coordinate> potentialNewPositions = Coordinate.getEmptyCellsWithFixedDistanceFrom(blockingObject.getCoordinate(), clearingDistance);
        //potentialNewPositions.addAll(edgeCells);

        // Prefer edge cells if it's the first attempt to clear this box
        // TODO: this value should be dynamic, depending on the level
        if (clearingDistance < 3 && !edgeCells.isEmpty()) {
            // Target --> Edge cell far away to the blocking box
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: edge cells chosen for %s", this.agent.getAgentId(), blockingObject));
            edgeCells.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, blockingObject.getCoordinate())));
            return (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);
        }

        // Target --> More distant among the cells with fixed distance from the blocking object
        potentialNewPositions.add(this.agent.getCoordinate());
        //potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, desire.getTarget())));
        potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, blockingObject.getCoordinate())));

        // Avoid choosing the same clearing target if it was already chosen in a previous (failed) iteration
        Coordinate chosenTarget;
        do {
            chosenTarget = (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);
            distances.remove(chosenTarget);
        }
        while (this.chosenTargetsForObjectToClear.containsKey(blockingObject) && this.chosenTargetsForObjectToClear.get(blockingObject).contains(chosenTarget) && chosenTarget != null);
        if (!this.chosenTargetsForObjectToClear.containsKey(blockingObject)) {
            Set<Coordinate> chosenTargets = new HashSet<>();
            this.chosenTargetsForObjectToClear.put(blockingObject, chosenTargets);
        }
        this.chosenTargetsForObjectToClear.get(blockingObject).add(chosenTarget);

        // Invalid target, will throw NoProgressException
        if (chosenTarget == null) {
            this.chosenTargetsForObjectToClear.remove(blockingObject);
            this.restoreClearingDistancesForAllObjects();
        }

        return chosenTarget;
    }

    public void clearChosenTargetsForObject(SokobanObject object) {
        this.chosenTargetsForObjectToClear.remove(object);
    }

    public void clearChosenTargetsForAllObjects() {
        this.chosenTargetsForObjectToClear.clear();
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
