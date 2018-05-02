package architecture.bdi;

import board.Box;
import board.Coordinate;
import planning.actions.ClearPathFromBoxTask;

import java.util.Objects;

public class ClearPathDesire implements Desire {

    private Box box;
    private Coordinate target;

    public ClearPathDesire(Box box, Coordinate target) {
        this.box = box;
        this.target = target;
    }

    @Override
    public Box getBox() {
        return this.box;
    }

    @Override
    public Coordinate getTarget() {
        return this.target;
    }

    @Override
    public Intention getIntention() {
        return new Intention(new ClearPathFromBoxTask());
    }

    @Override
    public String toString() {
        return "ClearPathFromBox{" + this.box.toString() + " --> " + this.target + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClearPathDesire)) return false;
        ClearPathDesire clearPathDesire = (ClearPathDesire) o;
        return target.equals(clearPathDesire.target);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(target);
    }
}
