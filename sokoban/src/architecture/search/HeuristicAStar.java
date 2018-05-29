package architecture.search;

import board.Coordinate;
import board.Level;

import java.util.Comparator;

public class HeuristicAStar implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        return this.f(n1) - this.f(n2);
    }

    public int f(Node n) {
        return n.g() + this.h(n);
    }

    public int h(Node n) {
        int penalty = Level.isGoalCell(n.getPosition()) && !Node.getGoal().getCoordinate().equals(n.getPosition()) ? 100 : 0;
        return Coordinate.manhattanDistance(n.getPosition(), Node.getGoal().getCoordinate()) + penalty;
    }
}
