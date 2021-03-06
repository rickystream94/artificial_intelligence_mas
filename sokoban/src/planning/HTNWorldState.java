package planning;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.Desire;
import board.Agent;
import board.Box;
import board.Coordinate;
import planning.actions.Direction;
import planning.actions.Effect;
import planning.actions.PrimitiveTask;
import planning.actions.PrimitiveTaskType;
import planning.relaxations.Relaxation;

import java.util.Objects;

/**
 * This class is in charge of tracking the state of relevant elements during HTN planning.
 */
public class HTNWorldState {

    private Agent agent;
    private Box box;
    private Coordinate target;
    private LevelManager levelManager;
    private Relaxation relaxation;

    /**
     * This constructor assumes that the color constraints between agent and box are satisfied
     * (Caller must verify this condition before instantiating a HTNWorldState)
     *
     * @param agent      current agent to track
     * @param desire     desire to achieve at the end of planning
     * @param relaxation relaxation for checking the preconditions
     */
    public HTNWorldState(Agent agent, Desire desire, Relaxation relaxation) {
        this.agent = new Agent(agent);
        this.relaxation = relaxation;
        this.levelManager = ClientManager.getInstance().getLevelManager();
        if (desire.getBox() != null)
            this.box = new Box(desire.getBox());
        this.target = desire.getTarget();
    }

    /**
     * Copy constructor
     *
     * @param other other world state to copy from
     */
    public HTNWorldState(HTNWorldState other) {
        this.levelManager = other.levelManager;
        this.agent = new Agent(other.agent);
        if (other.box != null)
            this.box = new Box(other.box);
        this.target = other.target;
        this.relaxation = other.relaxation;
    }


    public void applyEffect(Effect effect) {
        this.agent.setCoordinate(effect.getNewAgentPosition());
        Coordinate newBoxPosition = effect.getNewBoxPosition();
        if (newBoxPosition != null)
            this.box.setCoordinate(newBoxPosition);
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
        Coordinate targetPosition = Direction.getPositionByDirection(this.agent.getCoordinate(), dir1);
        return this.relaxation.movePreconditionsMet(targetPosition);
    }

    /**
     * 1st Precondition: from current agent's position towards dir1 there must be the box
     * 2nd Precondition: Box target position should be an empty cell
     *
     * @param dir1
     * @param dir2
     * @return
     */
    private boolean checkPushPreconditions(Direction dir1, Direction dir2) {
        Coordinate agentTargetPosition = Direction.getPositionByDirection(this.agent.getCoordinate(), dir1);
        Coordinate boxTargetPosition = Direction.getPositionByDirection(this.box.getCoordinate(), dir2);
        return this.relaxation.pushPreconditionsMet(agentTargetPosition, this.box.getCoordinate(), boxTargetPosition);
    }

    /**
     * 1st Precondition: cell in direction dir1 from current agent's position must be free
     * 2nd Precondition: Box target position must be equal to the current agent's position
     *
     * @param dir1
     * @param dir2
     * @return
     */
    private boolean checkPullPreconditions(Direction dir1, Direction dir2) {
        Coordinate agentTargetPosition = Direction.getPositionByDirection(this.agent.getCoordinate(), dir1);
        Coordinate boxTargetPosition = Direction.getPositionByDirection(this.box.getCoordinate(), Objects.requireNonNull(Direction.getOpposite(dir2)));
        return this.relaxation.pullPreconditionsMet(this.agent.getCoordinate(), agentTargetPosition, boxTargetPosition);
    }

    public Coordinate getAgentPosition() {
        return this.agent.getCoordinate();
    }

    public char getAgentId() {
        return this.agent.getAgentId();
    }

    public Coordinate getBoxPosition() {
        if (this.box != null)
            return this.box.getCoordinate();
        return null;
    }

    public Coordinate getTarget() {
        return this.target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HTNWorldState))
            return false;
        HTNWorldState s = (HTNWorldState) o;
        if (this.box != null)
            return this.agent.getCoordinate().equals(s.agent.getCoordinate()) &&
                    this.box.getCoordinate().equals(s.box.getCoordinate());
        return this.agent.getCoordinate().equals(s.agent.getCoordinate());
    }

    @Override
    public int hashCode() {
        if (this.box != null)
            return Objects.hash(this.agent.getCoordinate(), this.box.getCoordinate());
        else return Objects.hash(this.agent.getCoordinate());
    }
}
