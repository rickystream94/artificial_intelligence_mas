package planning;

import architecture.ClientManager;
import architecture.LevelManager;
import board.*;
import planning.actions.*;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * This class is in charge of tracking the state of relevant elements during HTN planning.
 */
public class HTNWorldState {

    private Agent agent;
    private Box box;
    private Goal goal;
    private LevelManager levelManager;

    /**
     * This constructor assumes that the color constraints between agent and box are satisfied
     * (Caller must verify this condition before instantiating a HTNWorldState)
     *
     * @param agent current agent to track
     * @param box   current box to track
     * @param goal  current goal to track
     */
    public HTNWorldState(Agent agent, Box box, Goal goal) {
        this.agent = agent;
        this.box = box;
        this.goal = goal;
        this.levelManager = ClientManager.getInstance().getLevelManager();
    }

    /**
     * Copy constructor
     *
     * @param other other world state to copy from
     */
    public HTNWorldState(HTNWorldState other) {
        this.levelManager = other.levelManager;
        this.agent = other.agent;
        this.box = other.box;
        this.goal = other.goal;
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
        /* TODO: possibility of improvements
        Intuitively, one might claim this return statement makes the trick:
        return this.levelManager.getLevel().isCellEmpty(targetPosition);
        The problem lies in the way the empty cells are (correctly!) stored: the Level instance contains information
        about the GLOBAL level, the one that always reflects the real situation, and not the one that each agent's planner
        thinks it is at the current planning step. Therefore, it will always include information about the position
        of the other agents/boxes (which will not be among the empty cells!). This will lead to a common
        (undesirable) situation: a primitive action's precondition might fail because a cell is reported "not empty"
        because another agent/box is occupying it (whereas, in reality, during the real plan execution,
        it might be occupied as well as it might not!)
        What's the solution? --> RELAXING THE PROBLEM! e.g. don't consider the other agents/boxes (dynamic entities),
        but only the walls (static entities)
        Currently, a relaxation where only walls are considered. But according to different situations,
        there might be different needs...
         */
        Coordinate targetPosition = Direction.getPositionByDirection(this.agent.getCoordinate(), dir1);
        return !Level.isWall(targetPosition);
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
        boolean isMet = this.box.getCoordinate().equals(agentTargetPosition); // 1st Precond, implies boxPosition is neighbour of agentPosition
        isMet = isMet && !Level.isWall(boxTargetPosition); // 2nd Precond
        return isMet;
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
        boolean isMet = Level.isWall(agentTargetPosition); // 1st Precond
        isMet = isMet && this.agent.getCoordinate().equals(boxTargetPosition); // 2nd Precond
        return isMet;
    }

    public Coordinate getAgentPosition() {
        return this.agent.getCoordinate();
    }

    public Coordinate getBoxPosition() {
        return this.agent.getCoordinate();
    }

    public Coordinate getGoalPosition() {
        return this.goal.getCoordinate();
    }

    /**
     * The current compound task should be refined in a list of 2 or more sub-tasks. It should avoid choosing already blacklisted refinements.
     *
     * @param compoundTask         Compound task to be refined
     * @param refinementsBlacklist Set of already blacklisted refinements
     * @param planningStep         Current planning step
     * @return A new refinement if any that satisfies the preconditions exists, null otherwise.
     */
    public Refinement findSatisfiedMethod(CompoundTask compoundTask, Set<Refinement> refinementsBlacklist, int planningStep) {
        // First step: refine task
        Queue<Refinement> foundRefinements = compoundTask.refineTask(this, planningStep);

        // Second step: avoid blacklisted refinements (if no valid refinements are found returns null!)
        Refinement refinement;
        do {
            refinement = foundRefinements.poll();
        }
        while (!refinementsBlacklist.contains(refinement));
        return refinement;
    }
}
