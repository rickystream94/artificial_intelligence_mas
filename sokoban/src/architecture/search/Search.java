package architecture.search;

import board.Coordinate;
import board.Goal;

import java.util.Deque;

public class Search {

    private StrategyBestFirst strategy;

    public Search(Coordinate from, Goal goal) {
        Node initialState = new Node(from, goal);
        this.strategy = new StrategyBestFirst(new HeuristicAStar());
        strategy.addToFrontier(initialState);
    }

    public Deque<Node> search() {
        while (true) {
            if (strategy.frontierIsEmpty()) {
                return null;
            }

            Node leafNode = strategy.getAndRemoveLeaf();
            if (leafNode.isGoalState()) {
                return leafNode.extractPlan();
            }

            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) {
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
        }
    }
}
