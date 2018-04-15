package planning.actions;

import board.Coordinate;

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
                return getPushEffect(currAgentPosition, currBoxPosition);
            case Pull:
                return getPullEffect(currAgentPosition, currBoxPosition);
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
        return this.actionType.toString() + "(" + dir1 + "," + dir2 + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof PrimitiveTask))
            return false;
        PrimitiveTask task = (PrimitiveTask) other;
        return this.dir1 == task.dir1 && this.dir2 == task.dir2 && this.actionType == task.actionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.actionType, this.dir1, this.dir2);
    }
}
