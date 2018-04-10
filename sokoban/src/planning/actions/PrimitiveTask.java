package planning.actions;

import java.util.LinkedList;
import java.util.Objects;

public class PrimitiveTask implements Task<PrimitiveTaskType> {

    static {
        LinkedList<PrimitiveTask> primitiveTasks = new LinkedList<>();

        primitiveTasks.add(new PrimitiveTask()); // NoOp

        for (Direction d : Direction.values()) {
            primitiveTasks.add(new PrimitiveTask(d)); // Move
        }

        for (Direction d1 : Direction.values()) {
            for (Direction d2 : Direction.values()) {
                if (!Direction.isOpposite(d1, d2)) {
                    primitiveTasks.add(new PrimitiveTask(PrimitiveTaskType.Push, d1, d2));
                }
            }
        }
        for (Direction d1 : Direction.values()) {
            for (Direction d2 : Direction.values()) {
                if (d1 != d2) {
                    primitiveTasks.add(new PrimitiveTask(PrimitiveTaskType.Pull, d1, d2));
                }
            }
        }

        every = primitiveTasks.toArray(new PrimitiveTask[0]);
    }

    public static final PrimitiveTask[] every;
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

    public Effect getEffect() {
        switch (this.actionType) {
            case Move:
                return getMoveEffect();
            case Push:
                return getPushEffect();
            case Pull:
                return getPullEffect();
            case NoOp:
                return null;
            default:
                return null;
        }
    }

    private Effect getPullEffect() {
        // TODO: to implement
        return null;
    }

    private Effect getPushEffect() {
        // TODO: to implement
        return null;
    }

    private Effect getMoveEffect() {
        // TODO: to implement
        return null;
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
