package planning.actions;

import board.Coordinate;
import board.Level;
import planning.HTNWorldState;

import java.util.Objects;

public class PrimitiveTask implements Task<PrimitiveTaskType> {

    private PrimitiveTaskType actionType;
    private Direction dir1;
    private Direction dir2;

    public PrimitiveTask() {
        this.actionType = PrimitiveTaskType.NoOp;
        dir1 = null;
        dir2 = null;
    }

    public PrimitiveTask(Direction d) {
        this.actionType = PrimitiveTaskType.Move;
        dir1 = d;
        dir2 = null;
    }

    public PrimitiveTask(PrimitiveTaskType actionType, Direction d1, Direction d2) {
        this.actionType = actionType;
        dir1 = d1;
        dir2 = d2;
    }

    public Effect getEffect(Coordinate currAgentPosition, Coordinate currBoxPosition) {
        switch (this.actionType) {
            case Move:
                return getMoveEffect(currAgentPosition);
            case Push:
                return getPushEffect(currAgentPosition, Objects.requireNonNull(currBoxPosition));
            case Pull:
                return getPullEffect(currAgentPosition, Objects.requireNonNull(currBoxPosition));
            case NoOp:
                return null;
            default:
                return null;
        }
    }

    private Effect getPullEffect(Coordinate currAgentPosition, Coordinate currBoxPosition) {
        Coordinate newAgentPosition = Direction.getPositionByDirection(currAgentPosition, dir1);
        Coordinate newBoxPosition = Direction.getPositionByDirection(currBoxPosition, Objects.requireNonNull(Direction.getOpposite(dir2)));
        return new Effect(newAgentPosition, newBoxPosition);
    }

    private Effect getPushEffect(Coordinate currAgentPosition, Coordinate currBoxPosition) {
        Coordinate newAgentPosition = Direction.getPositionByDirection(currAgentPosition, dir1);
        Coordinate newBoxPosition = Direction.getPositionByDirection(currBoxPosition, dir2);
        return new Effect(newAgentPosition, newBoxPosition);
    }

    private Effect getMoveEffect(Coordinate currAgentPosition) {
        Coordinate newAgentPosition = Direction.getPositionByDirection(currAgentPosition, dir1);
        return new Effect(newAgentPosition);
    }

    public Direction getDir1() {
        return this.dir1;
    }

    public Direction getDir2() {
        return this.dir2;
    }

    @Override
    public PrimitiveTaskType getType() {
        return this.actionType;
    }

    @Override
    public String toString() {
        if (this.actionType == PrimitiveTaskType.Move)
            return this.actionType.toString() + "(" + dir1 + ")";
        else if (this.actionType == PrimitiveTaskType.NoOp)
            return this.actionType.toString();
        return this.actionType.toString() + "(" + dir1 + "," + dir2 + ")";
    }

    @Override
    public int calculateApproximation(HTNWorldState worldState) {
        int cost = 0;

        // Apply effect of primitive action to the copy of the world state
        worldState.applyEffect(getEffect(worldState.getAgentPosition(), worldState.getBoxPosition()));

        // Manhattan Distance from box to goal and from agent to box
        // Agent and box should prefer staying close to walls
        if (worldState.getBoxPosition() != null) {
            cost += Coordinate.manhattanDistance(worldState.getBoxPosition(), worldState.getTarget());
            cost += Coordinate.manhattanDistance(worldState.getAgentPosition(), worldState.getBoxPosition());
            cost -= worldState.getBoxPosition().getClockwiseNeighbours().stream().filter(n -> !Level.isNotWall(n)).count();
        } else
            cost += Coordinate.manhattanDistance(worldState.getAgentPosition(), worldState.getTarget());
        cost -= worldState.getAgentPosition().getClockwiseNeighbours().stream().filter(n -> !Level.isNotWall(n)).count();
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveTask that = (PrimitiveTask) o;
        return actionType == that.actionType &&
                dir1 == that.dir1 &&
                dir2 == that.dir2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionType, dir1, dir2);
    }
}
