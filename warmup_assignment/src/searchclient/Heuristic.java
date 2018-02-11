package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

import searchclient.NotImplementedException;
import searchclient.Location;

public abstract class Heuristic implements Comparator<Node> {

	private HashMap<Character, HashMap<Location, Integer>> distancesPerGoalType = new HashMap<>();

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		System.err.println("Initializing heuristic, pre-processing static parts of the level...");

		// 1) Calculate list of different goal types
		HashSet<Character> goalTypesSet = new HashSet<>();
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				if (Node.goals[row][col] > 0) {
					goalTypesSet.add(Node.goals[row][col]);
				}
			}
		}
		System.err.print(goalTypesSet.size() + " goal type(s) found: ");
		goalTypesSet.stream().forEach(x -> System.err.print(x + " "));
		System.err.println();

		// 2) For each goal type found, create an entry in the distancesPerGoalType hashmap
		goalTypesSet.stream().forEach(x -> distancesPerGoalType.put(x, new HashMap<Location, Integer>()));

		// 3) For each cell and for each goal type, add a new entry in the distancesPerGoalType 
		// Example: goalType='a' for location=new Location(1,0) and distanceValue=3 --> distancesPerGoalType.get(goalType).put(location,distanceValue);
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				// Check if there is not a wall in (row,col)
				if (!Node.walls[row][col]) {
					for (char goalType : distancesPerGoalType.keySet()) {
						int distance = getDistanceToClosestGoal(row, col, goalType);
						distancesPerGoalType.get(goalType).put(new Location(row, col), distance);
					}
				}
			}
		}

		System.err.println("Pre-Processing done! Heuristic is ready.");
	}

	/**
	 * Given a location of a cell identified by (cellRow,cellCol), the method returns the Manhattan distance to the closest goal.
	 */
	private int getDistanceToClosestGoal(int cellRow, int cellCol, char goalType) {
		int dist, minDist = 1000;
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				// Check if there is a goal in goals[row][col]
				if (Node.goals[row][col] > 0 && Node.goals[row][col] == goalType) {
					dist = manhattanDistance(cellRow, cellCol, row, col);
					if (dist < minDist)
						minDist = dist;
				}
			}
		}
		return minDist;
	}

	/**
	* Given the location of the agent identified by (n.agentRow,n.agentCol), the method returns the Manhattan distance to the closest box.
	*/
	private int getDistanceToClosestBox(Node n) {
		int dist, minDist = 1000;
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				// Check if there is a box in boxes[row][col]
				if (n.boxes[row][col] > 0) {
					dist = manhattanDistance(n.agentRow, n.agentCol, row, col);
					if (dist < minDist)
						minDist = dist;
				}
			}
		}
		return minDist;
	}

	/**
	 * Calculates the Manhattan distance between two coordinates
	 */
	private int manhattanDistance(int row1, int col1, int row2, int col2) {
		return Math.abs((row1 - row2) + (col1 - col2));
	}

	public int h(Node n) {
		// 1) Sum of all distances between each box and its closest goal
		int costSum = 0;
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				if (n.boxes[row][col] > 0) {
					char goalType = Character.toLowerCase(n.boxes[row][col]);
					costSum += this.distancesPerGoalType.get(goalType).get(new Location(row, col));
				}
			}
		}

		// 2) Add distance from player to its closest box
		costSum += getDistanceToClosestBox(n);

		return costSum;
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int W) {
			super(initialState);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
