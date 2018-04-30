package architecture.bdi;

import board.Box;
import board.Coordinate;

public interface Desire {

    Box getBox();

    Coordinate getTarget();

    Intention getIntention();
}
