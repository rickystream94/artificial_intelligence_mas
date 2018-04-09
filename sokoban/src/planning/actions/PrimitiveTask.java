package planning.actions;

import java.util.LinkedList;

public class PrimitiveTask extends HighLevelTask {

    static {
        LinkedList<PrimitiveTask> primitiveTasks = new LinkedList<>();

        primitiveTasks.add(new PrimitiveTask()); // NoOp

        for (Direction d : Direction.values()) {
            primitiveTasks.add(new PrimitiveTask(d)); // Move
        }

        for (Direction d1 : Direction.values()) {
            for (Direction d2 : Direction.values()) {
                if (!PrimitiveTask.isOpposite(d1, d2)) {
                    primitiveTasks.add(new PrimitiveTask(ActionType.Push, d1, d2));
                }
            }
        }
        for (Direction d1 : Direction.values()) {
            for (Direction d2 : Direction.values()) {
                if (d1 != d2) {
                    primitiveTasks.add(new PrimitiveTask(ActionType.Pull, d1, d2));
                }
            }
        }

        every = primitiveTasks.toArray(new PrimitiveTask[0]);
    }

    public final static PrimitiveTask[] every;
    private ActionType actionType;
    private Direction dir1;
    private Direction dir2;

    public PrimitiveTask() {
        this.actionType = ActionType.NoOp;
        dir1 = null;
        dir2 = null;
    }

    public PrimitiveTask(Direction d) {
        this.actionType = ActionType.Move;
        dir1 = d;
        dir2 = null;
    }

    public PrimitiveTask(ActionType actionType, Direction d1, Direction d2) {
        this.actionType = actionType;
        dir1 = d1;
        dir2 = d2;
    }

    private static boolean isOpposite(Direction d1, Direction d2) {
        return d1.ordinal() + d2.ordinal() == 3;
    }

    public static int dirToRowChange(Direction d) {
        // South is down one row (1), north is up one row (-1).
        switch (d) {
            case S:
                return 1;
            case N:
                return -1;
            default:
                return 0;
        }
    }

    public static int dirToColChange(Direction d) {
        // East is right one column (1), west is left one column (-1).
        switch (d) {
            case E:
                return 1;
            case W:
                return -1;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        if (this.actionType == ActionType.Move)
            return this.actionType.toString() + "(" + dir1 + ")";
        return this.actionType.toString() + "(" + dir1 + "," + dir2 + ")";
    }
}
