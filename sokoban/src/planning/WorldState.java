package planning;

import architecture.ClientManager;
import architecture.LevelManager;
import board.Coordinate;
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
        // TODO: to implement
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

    private boolean checkMovePreconditions(Direction dir1) {
        // TODO: to implement. Depending on how we check the precondition, we might be implicitly relaxing the problem if e.g. we don't consider the other agents/boxes, but only the walls. Think about it...
        return false;
    }

    private boolean checkPushPreconditions(Direction dir1, Direction dir2) {
        // TODO: to implement
        return false;
    }

    private boolean checkPullPreconditions(Direction dir1, Direction dir2) {
        // TODO: to implement
        return false;
    }
}
