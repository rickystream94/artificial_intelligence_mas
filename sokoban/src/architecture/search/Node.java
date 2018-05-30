package architecture.search;

import board.Coordinate;
import board.Goal;
import board.Level;
import planning.actions.Direction;

import java.util.*;

public class Node {

    private static Goal goal;
    private Coordinate position;
    private Node parent;
    private int g;

    public Node(Coordinate position, Goal goal) {
        this.g = 0;
        this.position = position;
        Node.goal = goal;
    }

    public Node(Node parent, Coordinate newPosition) {
        this.parent = parent;
        this.g = parent.g + 1;
        this.position = newPosition;
    }

    public static Goal getGoal() {
        return Node.goal;
    }

    public Coordinate getPosition() {
        return position;
    }

    public int g() {
        return this.g;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public boolean isGoalState() {
        return this.position.equals(Node.goal.getCoordinate());
    }

    public List<Node> getExpandedNodes() {
        List<Node> expandedNodes = new ArrayList<>();
        for (Direction d : Direction.values()) {
            Coordinate newPosition = Direction.getPositionByDirection(position, d);
            if (Level.isNotWall(newPosition)) {
                Node childNode = new Node(this, newPosition);
                expandedNodes.add(childNode);
            }
        }
        Collections.shuffle(expandedNodes);
        return expandedNodes;
    }

    public Deque<Node> extractPlan() {
        Deque<Node> plan = new ArrayDeque<>();
        Node n = this;
        while (!n.isInitialState()) {
            plan.push(n);
            n = n.parent;
        }
        return plan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Node))
            return false;
        Node n = (Node) o;
        return n.position.equals(this.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position);
    }
}
