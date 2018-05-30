package architecture.search;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class StrategyBestFirst {

    private Set<Node> explored;
    private Queue<Node> frontier;
    private Set<Node> frontierSet;

    public StrategyBestFirst(HeuristicAStar heuristic) {
        this.explored = new HashSet<>();
        this.frontier = new PriorityQueue<>(heuristic);
        this.frontierSet = new HashSet<>();
    }

    public void addToExplored(Node n) {
        this.explored.add(n);
    }

    public boolean isExplored(Node n) {
        return this.explored.contains(n);
    }

    public Node getAndRemoveLeaf() {
        Node n = frontier.remove();
        frontierSet.remove(n);
        return n;
    }

    public void addToFrontier(Node n) {
        frontier.add(n);
        frontierSet.add(n);
    }

    public boolean frontierIsEmpty() {
        return frontier.isEmpty();
    }

    public boolean inFrontier(Node n) {
        return frontierSet.contains(n);
    }
}
