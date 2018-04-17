package architecture.bdi;

import board.Box;
import board.Goal;

public class Desire {
    private Box box;
    private Goal goal;

    public Desire(Box box, Goal goal) {
        this.box = box;
        this.goal = goal;
    }

    public Box getBox() {
        return box;
    }

    public Goal getGoal() {
        return goal;
    }
}
