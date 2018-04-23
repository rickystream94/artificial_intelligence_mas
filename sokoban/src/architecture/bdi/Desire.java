package architecture.bdi;

import board.Box;
import board.Goal;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Desire)) return false;
        Desire desire = (Desire) o;
        return box.equals(desire.box) && goal.equals(desire.goal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(box, goal);
    }

    @Override
    public String toString() {
        return this.box.toString() +
                " --> " +
                this.goal.toString();
    }
}
