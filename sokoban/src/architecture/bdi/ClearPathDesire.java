package architecture.bdi;

import board.Box;
import board.Coordinate;
import planning.actions.ClearPathFromBoxTask;

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
        return "ClearPathFromBox(" + this.box.toString() + ")";
    }
}
