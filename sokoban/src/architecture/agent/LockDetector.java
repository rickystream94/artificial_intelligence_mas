package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.BDIManager;
import architecture.bdi.ClearBoxDesire;
import architecture.bdi.ClearCellDesire;
import architecture.bdi.Desire;
import board.*;
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

    private Agent agent;
    private LevelManager levelManager;
    private int noProgressCounter;
    private Map<SokobanObject, Integer> objectClearingDistanceMap;
    private Map<SokobanObject, Set<Coordinate>> chosenTargetsForObjectToClear;
    private Deque<SokobanObject> blockingObjects;
    private Set<SokobanObject> falsePositivesBlockingObjects;
    private BDIManager bdiManager;

    public LockDetector(Agent agent) {
        this.agent = agent;
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.noProgressCounter = 0;
        this.objectClearingDistanceMap = new HashMap<>();
        this.chosenTargetsForObjectToClear = new HashMap<>();
        this.blockingObjects = new ArrayDeque<>();
        this.falsePositivesBlockingObjects = new HashSet<>();
        this.bdiManager = ClientManager.getInstance().getBdiManager();
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
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: False alarm, found stuck agent %c.", this.agent.getAgentId(), blockingAgent.getAgentId()));
                return;
            }*/ // TODO: temp removed: should check here if the blocking agent is the one I'm currently in conflict with

            // Ask for help
            throw new StuckByAgentException(agent, blockingAgent);
        } else if (blockingObject instanceof Box) {
            Box blockingBox = (Box) blockingObject;
            if (blockingBox.getColor() == this.agent.getColor()) {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Found blocking box %s", this.agent.getAgentId(), blockingBox));
                // Blocking box is of the same color
                // Prefer moving this box to a goal instead of clearing it
                if (this.bdiManager.canSolveGoalWithNoPriorityConflicts(blockingBox)) {
                    Set<Goal> goalsForBlockingBox = this.bdiManager.getUnsolvedGoalsForBox(blockingBox);
                    if (goalsForBlockingBox.size() > 0 && !this.falsePositivesBlockingObjects.contains(blockingBox)) {
                        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: box %s can be moved to a goal", this.agent.getAgentId(), blockingBox));
                        this.falsePositivesBlockingObjects.add(blockingBox);
                        return;
                    }
                }

                // Eventually remove false positive
                this.falsePositivesBlockingObjects.remove(blockingBox);

                // Clear the box
                noProgress();
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

    public void clearFalsePositiveBlockingObjects() {
        this.falsePositivesBlockingObjects.clear();
    }

    public Desire getDesireFromBlockingObject(SokobanObject blockingObject) throws NoAvailableTargetsException {
        Coordinate chosenTarget = findTargetForObjectToClear(blockingObject, true);

        if (blockingObject instanceof Box)
            return new ClearBoxDesire((Box) blockingObject, chosenTarget);
        else if (blockingObject instanceof Agent) {
            Box potentialBlockingBox = findNeighbourBlockingBox((Agent) blockingObject);
            if (potentialBlockingBox == null) {
                // Agent can free the cell without the need of clearing a box
                return new ClearCellDesire((Agent) blockingObject, chosenTarget);
            } else {
                // Agent can't move alone, it has to move a box
                return new ClearBoxDesire(potentialBlockingBox, chosenTarget);
            }
        }
        return null; // Unreachable
    }

    /**
     * If the agent has a neighbour blocking box he can move, return it. Otherwise return null
     *
     * @param blockingAgent
     * @return
     */
    private Box findNeighbourBlockingBox(Agent blockingAgent) {
        List<Coordinate> neighbours = blockingAgent.getCoordinate().getClockwiseNeighbours();
        int emptyNeighbours = levelManager.getLevel().countEmptyNeighbours(blockingAgent.getCoordinate());
        Agent helpingAgent = null;
        Box blockingBox = null;
        if (emptyNeighbours == 0) {
            for (Coordinate neighbour : neighbours) {
                SokobanObject object = levelManager.getLevel().dynamicObjectAt(neighbour);
                if (object instanceof Box && ((Box) object).getColor() == blockingAgent.getColor())
                    blockingBox = (Box) object;
                if (object instanceof Agent)
                    helpingAgent = (Agent) object;
            }
        }
        if (helpingAgent == null && blockingBox != null)
            return blockingBox;
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

        // Prefer edge cells if still possible
        if (!edgeCells.isEmpty()) {
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
        //potentialNewPositions.add(this.agent.getCoordinate()); // TODO: temp removed
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
        saveChoiceOfTargetForObjectToClear(blockingObject, chosenTarget);

        return chosenTarget;
    }

    public void saveChoiceOfTargetForObjectToClear(SokobanObject object, Coordinate target) {
        if (target != null) {
            if (!this.chosenTargetsForObjectToClear.containsKey(object)) {
                Set<Coordinate> chosenTargets = new HashSet<>();
                this.chosenTargetsForObjectToClear.put(object, chosenTargets);
            }
            this.chosenTargetsForObjectToClear.get(object).add(target);
        }
    }

    public void clearChosenTargetsForAllObjects() {
        this.chosenTargetsForObjectToClear.clear();
    }

    public void clearChosenTargetsForObject(SokobanObject blockingObject) {
        this.chosenTargetsForObjectToClear.remove(blockingObject);
    }

    private Set<Coordinate> getEmptyEdgeCells() {
        Set<Coordinate> emptyCells = levelManager.getLevel().getEmptyCellsPositions();
        return emptyCells.stream().filter(c -> Coordinate.isEdgeCell(c, true)).collect(Collectors.toSet());
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
