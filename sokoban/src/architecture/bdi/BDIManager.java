package architecture.bdi;

import architecture.ClientManager;
import board.*;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BDIManager {

    private static final Logger LOGGER = ConsoleLogger.getLogger(BDIManager.class.getSimpleName());

    public Map<Agent, FibonacciHeap<Desire>> generateDesires() {
        ConsoleLogger.logInfo(LOGGER, "Generating desires...");

        // Step 0: Prepare
        Map<Agent, FibonacciHeap<Desire>> agentDesiresMap = new HashMap<>();
        Level level = ClientManager.getInstance().getLevelManager().getLevel();
        Set<Agent> agents = new HashSet<>(level.getAgents());
        Set<Goal> goals = new HashSet<>(Level.getGoals());
        Set<Box> boxes = new HashSet<>(level.getBoxes());
        List<Desire> desires = new ArrayList<>();
        agents.forEach(agent -> agentDesiresMap.put(agent, new FibonacciHeap<>()));

        // Step 1: Map each type to the list of boxes/goals belonging to that type
        Map<Character, List<Box>> boxesPerTypeMap = new HashMap<>();
        Map<Character, List<Goal>> goalsPerTypeMap = new HashMap<>();
        for (Box box : boxes) {
            char boxType = Character.toLowerCase(box.getBoxType());
            if (!boxesPerTypeMap.containsKey(boxType))
                boxesPerTypeMap.put(boxType, new ArrayList<>());
            boxesPerTypeMap.get(boxType).add(box);
        }
        for (Goal goal : goals) {
            char goalType = goal.getGoalType();
            if (!goalsPerTypeMap.containsKey(goalType))
                goalsPerTypeMap.put(goalType, new ArrayList<>());
            goalsPerTypeMap.get(goalType).add(goal);
        }
        ConsoleLogger.logInfo(LOGGER, "Step 1: Done mapping boxes and goals to their type");

        // Step 2: Box-Goal assignment --> Which box goes to which goal? --> Output = List of Desires
        for (Character type : goalsPerTypeMap.keySet()) {
            // Create a desire for each goal
            while (!goalsPerTypeMap.get(type).isEmpty()) {
                Map<Object, Integer> desireCostMap = new HashMap<>();
                for (Box box : boxesPerTypeMap.get(type)) {
                    for (Goal goal : goalsPerTypeMap.get(type)) {
                        int cost = getCostBetweenObjects(box, goal);
                        cost += getPunishment(goal);
                        desireCostMap.put(new Desire(box, goal), cost);
                    }
                }
                Desire minCostDesire = (Desire) getKeyByMinValue(desireCostMap);
                desires.add(minCostDesire);

                // Remove occurrence of chosen box and goal for the current desire
                boxesPerTypeMap.get(type).remove(minCostDesire.getBox());
                goalsPerTypeMap.get(type).remove(minCostDesire.getGoal());
            }
        }
        // Testing purposes --> All goals should be assigned to a box (there can be, instead, a box not assigned to any goal)
        assert goalsPerTypeMap.values().stream().allMatch(List::isEmpty);
        ConsoleLogger.logInfo(LOGGER, "Step 2: Done creating desires as Box-Goal mappings");

        // Step 3: Create support data structure to keep track of workload of each agent
        Map<Color, Map<Object, Integer>> workloadOfAgentsByColor = new HashMap<>();
        for (Color c : agents.stream().map(Agent::getColor).collect(Collectors.toList())) {
            workloadOfAgentsByColor.put(c, new HashMap<>());
            agents.stream().filter(agent -> agent.getColor() == c).forEach(agent -> workloadOfAgentsByColor.get(c).put(agent, 0));
        }
        ConsoleLogger.logInfo(LOGGER, "Step 3: Done mapping boxes and goals to their type");

        // Step 4: Assign Desires  to agents keeping the workload well spread among them
        while (!desires.isEmpty()) {
            Desire currentDesire = desires.remove(0);
            List<Agent> agentsOfColor = agents.stream().filter(agent -> agent.getColor() == currentDesire.getBox().getColor()).collect(Collectors.toList());
            Map<Object, Integer> desireCostByAgent = new HashMap<>(); // Each agent's cost to reach the desire's box
            agentsOfColor.forEach(agent -> desireCostByAgent.put(agent, getCostBetweenObjects(agent, currentDesire.getBox())));
            Agent chosenAgent = (Agent) getKeyByMinValue(desireCostByAgent);

            // Check if current workload of chosen agent is above the minimum
            Map<Object, Integer> agentsWorkload = workloadOfAgentsByColor.get(currentDesire.getBox().getColor());
            if (agentsWorkload.get(chosenAgent) > agentsWorkload.values().stream().min(Integer::compareTo).get()) {
                // Desire is assigned to the agent with the lowest workload
                chosenAgent = (Agent) getKeyByMinValue(agentsWorkload);
            }

            // Increment workload of chosen agent
            agentsWorkload.put(chosenAgent, agentsWorkload.get(chosenAgent) + 1);

            // Assign desire to chosen agent
            agentDesiresMap.get(chosenAgent).enqueue(currentDesire, (double) desireCostByAgent.get(chosenAgent));
        }
        ConsoleLogger.logInfo(LOGGER, "Step 4: Done (semi-optimally) assigning desires to agents");

        return agentDesiresMap;
    }

    private int getPunishment(SokobanObject object) {
        int punishment = 0;
        for (Coordinate coordinate : object.getCoordinate().getNeighbours()) {
            if (!Level.isNotWall(coordinate)) // It's a wall
                punishment++;
            if (Level.isGoalCell(coordinate))
                punishment--;
        }
        return punishment;
    }

    /**
     * Given a map with generic keys and integer values, returns the key that has the minimum value
     *
     * @param map Generic Object-Integer map
     * @return key corresponding to minimum value
     */
    private Object getKeyByMinValue(Map<Object, Integer> map) {
        int minValue = Integer.MAX_VALUE;
        Object chosenKey = null;
        for (Object key : map.keySet()) {
            int value = map.get(key);
            if (value < minValue) {
                minValue = value;
                chosenKey = key;
            }
        }
        return chosenKey;
    }

    private int getCostBetweenObjects(SokobanObject o1, SokobanObject o2) {
        return Coordinate.manhattanDistance(o1.getCoordinate(), o2.getCoordinate());
    }
}
