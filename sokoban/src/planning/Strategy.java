package planning;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Stack;

public abstract class Strategy {
	private HashSet<HTNNode> explored;
	private final long startTime;

	public Strategy() {
		this.explored = new HashSet<HTNNode>();
		this.startTime = System.currentTimeMillis();
	}

	public void addToExplored(HTNNode n) {
		this.explored.add(n);
	}

	public boolean isExplored(HTNNode n) {
		return this.explored.contains(n);
	}

	public int countExplored() {
		return this.explored.size();
	}

	public String searchStatus() {
		return String.format("#Explored: %,6d, #Frontier: %,6d, #Generated: %,6d, Time: %3.2f s", this.countExplored(), this.countFrontier(), this.countExplored()+this.countFrontier(), this.timeSpent());
	}

	public float timeSpent() {
		return (System.currentTimeMillis() - this.startTime) / 1000f;
	}

	public abstract HTNNode getAndRemoveLeaf();

	public abstract void addToFrontier(HTNNode n);

	public abstract boolean inFrontier(HTNNode n);

	public abstract int countFrontier();

	public abstract boolean frontierIsEmpty();

	@Override
	public abstract String toString();

	public static class StrategyBFS extends Strategy {
		private ArrayDeque<HTNNode> frontier;
		private HashSet<HTNNode> frontierSet;

		public StrategyBFS() {
			super();
			frontier = new ArrayDeque<HTNNode>();
			frontierSet = new HashSet<HTNNode>();
		}

		@Override
		public HTNNode getAndRemoveLeaf() {
			HTNNode n = frontier.pollFirst();
			frontierSet.remove(n);
			return n;
		}

		@Override
		public void addToFrontier(HTNNode n) {
			frontier.addLast(n);
			frontierSet.add(n);
		}

		@Override
		public int countFrontier() {
			return frontier.size();
		}

		@Override
		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		@Override
		public boolean inFrontier(HTNNode n) {
			return frontierSet.contains(n);
		}

		@Override
		public String toString() {
			return "Breadth-first Search";
		}
	}

	public static class StrategyDFS extends Strategy {
		private Stack<HTNNode> frontier;
		private HashSet<HTNNode> frontierSet;

		public StrategyDFS() {
			super();
			frontier = new Stack<HTNNode>();
			frontierSet = new HashSet<HTNNode>();
		}

		@Override
		public HTNNode getAndRemoveLeaf() {
			HTNNode n = frontier.pop();
			frontierSet.remove(n);
			return n;
		}

		@Override
		public void addToFrontier(HTNNode n) {
			frontier.push(n);
			frontierSet.add(n);
		}

		@Override
		public int countFrontier() {
			return frontier.size();	
		}

		@Override
		public boolean frontierIsEmpty() {
			return frontier.isEmpty();	
		}

		@Override
		public boolean inFrontier(HTNNode n) {
			return frontierSet.contains(n);
		}

		@Override
		public String toString() {
			return "Depth-first Search";
		}
	}
}
