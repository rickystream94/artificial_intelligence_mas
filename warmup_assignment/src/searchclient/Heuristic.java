package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

import searchclient.NotImplementedException;
import searchclient.Location;

public abstract class Heuristic implements Comparator<Node> {

	/**
	 * This apparently complicated field is in charge of holding, for each cell, its distance from the closest goal for each goal type.
	 */
	private HashMap<Character, HashMap<Location, Integer>> minDistancesFromGoals = new HashMap<>();

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

		// 2) For each goal type found, create an entry in the minDistancesFromGoals hashmap
		goalTypesSet.stream().forEach(x -> minDistancesFromGoals.put(x, new HashMap<Location, Integer>()));

		// 3) For each cell and for each goal type, add a new entry in the minDistancesFromGoals 
		// Example: goalType='a' for location = new Location(1,0) and distanceValue = 3
		// --> minDistancesFromGoals.get(goalType).put(location,distanceValue);
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				// Check if there is not a wall in walls[row][col]
				if (!Node.walls[row][col]) {
					for (char goalType : minDistancesFromGoals.keySet()) {
						int distance = distanceCellToClosestGoal(row, col, goalType);
						minDistancesFromGoals.get(goalType).put(new Location(row, col), distance);
					}
				}
			}
		}

		System.err.println("Pre-Processing done! Heuristic is ready.");
	}

	/**
	 * Given a location of a cell identified by (cellRow,cellCol), the method returns the Manhattan distance to the closest goal of type goalType.
	 * @param cellRow row of the cell
	 * @param cellCol column of the cell
	 * @param goalType type of the goal (e.g. 'a', 'b', 'c'...)
	 * @return Manhattan distance between the cell and the closest goal of type goalType
	 */
	private int distanceCellToClosestGoal(int cellRow, int cellCol, char goalType) {
		int dist, minDist = 1000;
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				// Check if there is a goal in goals[row][col]
				if (Node.goals[row][col] == goalType) {
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
	/* private int distanceAgentToItsClosestBox(Node n) {
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
	} */

	/**
	 * Given the current positions of the boxes, returns the distance between the agent and the box closest to the goal of the same type
	 * @param n Input node that holds the current positions of the boxes (n.boxes[...][...]) and the position of the agent
	 * @return Distance between the agent and the box closest to the goal of the same type
	 */
	private int distanceAgentAndBoxClosestToItsGoal(Node n) {
		int boxRow = 0, boxCol = 0, dist, minDist = 1000;
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				if (n.boxes[row][col] > 0) {
					char goalType = Character.toLowerCase(n.boxes[row][col]);
					dist = this.minDistancesFromGoals.get(goalType).get(new Location(row, col));
					if (dist < minDist) {
						boxRow = row;
						boxCol = col;
						minDist = dist;
					}
				}
			}
		}
		return manhattanDistance(n.agentRow, n.agentCol, boxRow, boxCol);
	}

	/**
	 * @param n input node
	 * @return The sum of the distances between each box and the closest goal of the same type (e.g. Box_A and goal 'a' are of the same type)
	 */
	private int sumOfDistancesEachBoxAndItsClosestGoal(Node n) {
		int sum = 0;
		for (int row = 0; row < Node.rowSize; row++) {
			for (int col = 0; col < Node.colSize; col++) {
				if (n.boxes[row][col] > 0) {
					char goalType = Character.toLowerCase(n.boxes[row][col]);
					sum += this.minDistancesFromGoals.get(goalType).get(new Location(row, col));
				}
			}
		}
		return sum;
	}

	/**
	 * Calculates the Manhattan distance between two coordinates
	 */
	private int manhattanDistance(int row1, int col1, int row2, int col2) {
		return Math.abs((row1 - row2) + (col1 - col2));
	}

	public int h(Node n) {
		int cost = 0;

		// 1) Sum of all distances between each box and its closest goal
		cost += sumOfDistancesEachBoxAndItsClosestGoal(n);

		// 2) Add distance from player to its closest box --> not really helpful
		//cost += distanceAgentToItsClosestBox(n);

		// 3) Add distance from player and the box closest to the goal
		cost += distanceAgentAndBoxClosestToItsGoal(n);

		return cost;
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
