package planning;

import architecture.ClientManager;
import architecture.LevelManager;
import board.Coordinate;
import board.Level;
import planning.actions.Direction;
import planning.actions.Effect;
import planning.actions.PrimitiveTask;
import planning.actions.PrimitiveTaskType;

public class WorldState {
    private Coordinate agentPosition;
    private Coordinate boxPosition;
    private LevelManager levelManager;

    public WorldState(Coordinate agentPosition, Coordinate boxPosition) {
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.agentPosition = agentPosition;
        this.boxPosition = boxPosition;
    }

    /**
     * Copy constructor
     *
     * @param other other world state to copy from
     */
    public WorldState(WorldState other) {
        this.levelManager = other.levelManager;
        this.agentPosition = other.agentPosition;
        this.boxPosition = other.boxPosition;
    }


    public void applyEffect(Effect effect) {
        this.agentPosition = effect.getNewAgentPosition();
        Coordinate newBoxPosition = effect.getNewBoxPosition();
        if (newBoxPosition != null)
            this.boxPosition = newBoxPosition;
    }

    public boolean preconditionsMet(PrimitiveTask task) {
        PrimitiveTaskType taskType = task.getType();
        switch (taskType) {
            case NoOp:
                return true;
            case Pull:
                return checkPullPreconditions(task.getDir1(), task.getDir2());
            case Push:
                return checkPushPreconditions(task.getDir1(), task.getDir2());
            case Move:
                return checkMovePreconditions(task.getDir1());
            default:
                return false;
        }
    }

    /**
     * Precondition: target position must be empty (no wall)
     *
     * @param dir1 direction where agent should move
     * @return true if all preconditions are met
     */
    private boolean checkMovePreconditions(Direction dir1) {
        /* TODO
        Intuitively, one might claim this return statement makes the trick:
        return this.levelManager.getLevel().isCellEmpty(targetPosition);
        The problem lies in the way the empty cells are (correctly!) stored: the Level instance contains information
        about the GLOBAL level, the one that always reflects the real situation, and not the one that each agent's planner
        thinks it is. Therefore, it will always include information about the position of the other agents/boxes
        (which will not be among the empty cells!). This will lead to a common (undesirable) situation: a primitive
        action's precondition might fail because a cell is reported "not empty" because another agent/box is occupying it
        (whereas, in reality, during the real plan execution, it might be occupied as well as it might not!)
        What's the solution? --> RELAXING THE PROBLEM! e.g. don't consider the other agents/boxes (dynamic entities),
        but only the walls (static entities)
        Currently, a relaxation where only walls are considered. But according to different situations,
        there might be different needs...
         */
        Coordinate targetPosition = Direction.getPositionByDirection(this.agentPosition, dir1);
        return Level.getWalls().stream().noneMatch(wall -> wall.getCoordinate().equals(targetPosition));
    }

    /**
     * 1st Precondition: from current's agent position towards dir1 there must be a box
     * 2nd Precondition: Box target position should be an empty cell
     *
     * @param dir1
     * @param dir2
     * @return
     */
    private boolean checkPushPreconditions(Direction dir1, Direction dir2) {
        Coordinate agentTargetPosition = Direction.getPositionByDirection(this.agentPosition, dir1);
        Coordinate boxTargetPosition = Direction.getPositionByDirection(this.boxPosition, dir2);
        boolean isMet = this.boxPosition.equals(agentTargetPosition); // 1st Precond, implies boxPosition is neighbour of agentPosition
        isMet = isMet && Level.getWalls().stream().noneMatch(wall -> wall.getCoordinate().equals(boxTargetPosition)); // 2nd Precond
        return isMet;
    }

    private boolean checkPullPreconditions(Direction dir1, Direction dir2) {
        Coordinate agentTargetPosition = Direction.getPositionByDirection(this.agentPosition, dir1);
        Coordinate boxTargetPosition = Direction.getPositionByDirection(this.boxPosition, dir2);
    }
}
