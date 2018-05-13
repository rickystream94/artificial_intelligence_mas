package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.ClearBoxDesire;
import architecture.bdi.ClearCellDesire;
import architecture.bdi.Desire;
import board.Agent;
import board.Box;
import board.Coordinate;
import board.SokobanObject;
import exceptions.NoAvailableTargetsException;
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
    private static final int DEFAULT_CLEARING_DISTANCE = 1;
    private static final int MAX_NO_PROGRESS_COUNTER = 50;
    private static final int CLEARING_DISTANCE_THRESHOLD = 3;

    private Agent agent;
    private LevelManager levelManager;
    private int noProgressCounter;
    private Map<SokobanObject, Integer> objectClearingDistanceMap;
    private Map<SokobanObject, Set<Coordinate>> chosenTargetsForObjectToClear;
    private Deque<SokobanObject> blockingObjects;

    public LockDetector(Agent agent) {
        this.agent = agent;
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.noProgressCounter = 0;
        this.objectClearingDistanceMap = new HashMap<>();
        this.chosenTargetsForObjectToClear = new HashMap<>();
        this.blockingObjects = new ArrayDeque<>();
    }

    protected int getClearingDistance(SokobanObject object) {
        if (!this.objectClearingDistanceMap.containsKey(object))
            this.objectClearingDistanceMap.put(object, DEFAULT_CLEARING_DISTANCE);
        return this.objectClearingDistanceMap.get(object);
    }

    public void incrementClearingDistance(SokobanObject object) {
        int currentClearingDistance = getClearingDistance(object);
        this.objectClearingDistanceMap.put(object, currentClearingDistance + 1);
    }

    public void restoreClearingDistancesForAllObjects() {
        this.objectClearingDistanceMap.clear();
    }

    public void restoreClearingDistanceForObject(SokobanObject object) {
        this.objectClearingDistanceMap.remove(object);
    }

    /**
     * This method is in charge of letting the agent figure out whether he can solve the blocking issue on its own or
     * he needs to ask for help
     *
     * @param failedAction  last action that failed
     * @param currentDesire current desire to achieve
     */
    public void detectBlockingObject(PrimitiveTask failedAction, Desire currentDesire) throws StuckByForeignBoxException, StuckByAgentException, NoProgressException {
        Coordinate blockingCell = getBlockingCellByAction(failedAction, currentDesire);
        SokobanObject blockingObject = this.levelManager.getLevel().dynamicObjectAt(Objects.requireNonNull(blockingCell));

        // Analyze blocking object and decide
        if (blockingObject instanceof Agent) {
            Agent blockingAgent = (Agent) blockingObject;
            /*if (blockingAgent.getStatus() == AgentStatus.STUCK) {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: False alarm, found stuck agent %c. I'll recalculate my target...", this.agent.getAgentId(), blockingAgent.getAgentId()));
                return;
            }*/ // TODO: temp removed: should check here if the blocking agent is the one I'm currently in conflict with

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

    public Desire getDesireFromBlockingObject(SokobanObject blockingObject) throws NoAvailableTargetsException {
        Coordinate chosenTarget = findTargetForObjectToClear(blockingObject, true);

        if (blockingObject instanceof Box)
            return new ClearBoxDesire((Box) blockingObject, chosenTarget);
        else if (blockingObject instanceof Agent)
            return new ClearCellDesire((Agent) blockingObject, chosenTarget);
        // TODO the agent might not be able to free the cell without moving a box! Check the neighbours (should fix MAsimple5)

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

    /**
     * Records the blocking object, if not already stored previously
     *
     * @param blockingObject blocking object
     */
    public void addBlockingObject(SokobanObject blockingObject) {
        if (this.blockingObjects.stream().noneMatch(object -> object == blockingObject))
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
     * @throws NoAvailableTargetsException if no valid target could be found
     */
    public Coordinate findTargetForObjectToClear(SokobanObject blockingObject, boolean shouldLog) throws NoAvailableTargetsException {
        Coordinate chosenTarget;
        int clearingDistance = getClearingDistance(blockingObject);
        if (shouldLog)
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: clearing distance for %s is %d.", this.agent.getAgentId(), blockingObject, clearingDistance));
        Set<Coordinate> edgeCells = getEmptyEdgeCells();

        // Prefer edge cells if it's the first attempt to clear this box
        if (clearingDistance < CLEARING_DISTANCE_THRESHOLD && !edgeCells.isEmpty()) {
            // Target --> Edge cell far away to the blocking box
            if (shouldLog)
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: edge cells chosen for %s", this.agent.getAgentId(), blockingObject));
            chosenTarget = chooseAndValidateBestTarget(blockingObject, edgeCells);

            // If chosen target is valid, return. Otherwise, try with the other potential new targets
            if (chosenTarget != null)
                return chosenTarget;
        }

        // If no edge cells are available or are not valid, calculate the targets
        // with fixed distance from the blocking object
        Set<Coordinate> potentialNewPositions = Coordinate.getEmptyCellsWithFixedDistanceFrom(blockingObject.getCoordinate(), clearingDistance);
        potentialNewPositions.add(this.agent.getCoordinate());
        chosenTarget = chooseAndValidateBestTarget(blockingObject, potentialNewPositions);

        // Check if the target is valid
        if (chosenTarget != null)
            return chosenTarget;

        // If we reach this point, chosenTarget is null: throw NoProgressException
        throw new NoAvailableTargetsException(this.agent, blockingObject);
    }

    /**
     * Chooses the best target according to distance heuristic: the most distant from the blocking object is preferred.
     * Iteratively validates the chosen target(s), discarding them and trying with the next one
     * if the target was already chosen in a previous attempt to clear the same object
     *
     * @param blockingObject   object to clear
     * @param potentialTargets set of potential new targets
     * @return the new target if valid, null otherwise
     */
    private Coordinate chooseAndValidateBestTarget(SokobanObject blockingObject, Set<Coordinate> potentialTargets) {
        Coordinate chosenTarget;
        Map<Object, Integer> distances = new HashMap<>();

        // Calculate distances from blocking object to the potential targets
        potentialTargets.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, blockingObject.getCoordinate())));

        // Choose the most distant target, if not already chosen before
        do {
            chosenTarget = (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);
            distances.remove(chosenTarget);
        }
        while (this.chosenTargetsForObjectToClear.containsKey(blockingObject) && this.chosenTargetsForObjectToClear.get(blockingObject).contains(chosenTarget) && chosenTarget != null);

        // Back-up choice of target
        if (chosenTarget != null) {
            if (!this.chosenTargetsForObjectToClear.containsKey(blockingObject)) {
                Set<Coordinate> chosenTargets = new HashSet<>();
                this.chosenTargetsForObjectToClear.put(blockingObject, chosenTargets);
            }
            this.chosenTargetsForObjectToClear.get(blockingObject).add(chosenTarget);
        }

        return chosenTarget;
    }

    public void clearChosenTargetsForAllObjects() {
        this.chosenTargetsForObjectToClear.clear();
    }

    public void clearChosenTargetsForObject(SokobanObject blockingObject) {
        this.chosenTargetsForObjectToClear.remove(blockingObject);
    }

    private Set<Coordinate> getEmptyEdgeCells() {
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
        if (result) {
            progressPerformed();
        }
        return result;
    }
}
