package architecture.bdi;

import board.Box;
import board.Coordinate;
import planning.actions.ClearPathFromBoxTask;

import java.util.Objects;

public class ClearBoxDesire implements Desire {

    private Box box;
    private Coordinate target;

    public ClearBoxDesire(Box box, Coordinate target) {
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
        return "ClearBox{" + this.box.toString() + " --> " + this.target + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClearBoxDesire)) return false;
        ClearBoxDesire clearBoxDesire = (ClearBoxDesire) o;
        return target.equals(clearBoxDesire.target) && box.equals(clearBoxDesire.box);
    }

    @Override
    public int hashCode() {
        return Objects.hash(box, target);
    }
}
