package architecture.bdi;

import architecture.ClientManager;
import board.*;
import javafx.util.Pair;
import planning.actions.CompoundTask;
import utils.FibonacciHeap;
import utils.FibonacciHeap.Entry;

import java.util.*;

public class BDIManager {

    public Map<Agent, FibonacciHeap<Desire>> generateDesires() {
        // Prepare
        Map<Agent, FibonacciHeap<Desire>> actionsByAgent = new HashMap<>();
        Level level = ClientManager.getInstance().getLevelManager().getLevel();
        Set<Agent> agents = new HashSet<>(level.getAgents());
        Set<Goal> goals = new HashSet<>(Level.getGoals());
        Set<Box> boxes = new HashSet<>(level.getBoxes());

        // Map each type to the list of boxes/goals belonging to that type
        Map<Character, List<Box>> boxesPerTypeMap = new HashMap<>();
        Map<Character, List<Goal>> goalsPerTypeMap = new HashMap<>();
        for (Box box : boxes) {
            if (!boxesPerTypeMap.containsKey(box.getBoxType()))
                boxesPerTypeMap.put(box.getBoxType(), new ArrayList<>());
            List<Box> boxesOfType = boxesPerTypeMap.get(box.getBoxType());
            boxesOfType.add(box);
            boxesPerTypeMap.put(box.getBoxType(), boxesOfType);
        }
        for (Goal goal : goals) {
            if (!goalsPerTypeMap.containsKey(goal.getGoalType()))
                goalsPerTypeMap.put(goal.getGoalType(), new ArrayList<>());
            List<Goal> goalsOfType = goalsPerTypeMap.get(goal.getGoalType());
            goalsOfType.add(goal);
            goalsPerTypeMap.put(goal.getGoalType(), goalsOfType);
        }

        // Step 1: Box-Goal assignment --> Which box goes to which goal? --> Output = List of Desires
        List<Desire> desires = new ArrayList<>();
        for (Character type : goalsPerTypeMap.keySet()) {
            List<Box> boxesOfType = boxesPerTypeMap.get(type);
            List<Goal> goalsOfType = goalsPerTypeMap.get(type);
            // Create a desire for each goal
            while (!goalsOfType.isEmpty()) {
                Map<Desire, Integer> desiresCost = new HashMap<>();
                for (Box box : boxesOfType) {
                    for (Goal goal : goalsOfType) {
                        int cost = Coordinate.manhattanDistance(box.getCoordinate(), goal.getCoordinate());
                        desiresCost.put(new Desire(box, goal), cost);
                    }
                }
                Desire minCostDesire = getKeyByMinValue(desiresCost);
                desires.add(minCostDesire);

                // Remove occurrence of chosen box and goal for the current desire
                boxesOfType.remove(minCostDesire.getBox());
                goalsOfType.remove(minCostDesire.getGoal());
                boxesPerTypeMap.put(type, boxesOfType);
                goalsPerTypeMap.put(type, goalsOfType);
            }
        }
        // Testing purposes --> All goals should be assigned to a box (there can be, instead, a box not assigned to any goal)
        assert goalsPerTypeMap.values().stream().allMatch(List::isEmpty);

        // Step 2: Desire assignment to agents
        for (Agent agent : agents) {
            List<Box> boxesForAgent = agentAndBoxesMap.get(agent);
            for (Box box : boxesForAgent) {
                if (!boxAndGoalsMap.containsKey(box))
                    continue;
                List<Goal> goalsForBox = boxAndGoalsMap.get(box);
                for (Goal goal : goalsForBox) {
                    double dist = getActionCost(agent, box, goal);
                    Pair<Box, Goal> action = new Pair<>(box, goal);
                    if (!actionsByAgent.containsKey(action)) {
                        actionsByAgent.put(action, new Pair<>(agent, dist));
                    } else {
                        Pair<Agent, Double> responsible = actionsByAgent.get(action);
                        double cost = responsible.getValue();
                        if (cost > dist) {
                            actionsByAgent.put(action, new Pair<>(agent, dist));
                        }
                    }
                }
            }
        }

        for (Map.Entry<Pair<Box, Goal>, Pair<Agent, Double>> entry : actionsByAgent.entrySet()) {
            Agent agent = entry.getValue().getKey();
            Double cost = entry.getValue().getValue();
            if (!this.actionsByAgent.containsKey(agent))
                this.actionsByAgent.put(agent, new FibonacciHeap<>());
            FibonacciHeap<Pair<Box, Goal>> actions = this.actionsByAgent.get(agent);
            actions.enqueue(entry.getKey(), cost);
        }
    }

    private Desire getKeyByMinValue(Map<Desire, Integer> desiresCost) {
        int minValue = Integer.MAX_VALUE;
        Desire chosenDesire = null;
        for (Desire desire : desiresCost.keySet()) {
            int value = desiresCost.get(desire);
            if (value < minValue) {
                minValue = value;
                chosenDesire = desire;
            }
        }
        return chosenDesire;
    }

    public CompoundTask getCompoundTaskByAgent(Agent agent) {
        /*
        FibonacciHeap<CompoundTask> compoundTaskHeap = actionsByAgent.get(agent);

        if(compoundTaskHeap != null) {
            Entry<CompoundTask> compoundTask = compoundTaskHeap.dequeueMin();
            if(compoundTask != null)
                return compoundTask.getValue();
        }
        */
        return null;
    }

    /**
     * This function fucks up the FibonacciHeap
     */
    private void printActions() {
        for (Map.Entry<Agent, FibonacciHeap<Pair<Box, Goal>>> entry : this.actionsByAgent.entrySet()) {
            Agent agent = entry.getKey();
            FibonacciHeap<Pair<Box, Goal>> actions = entry.getValue();

            while (!actions.isEmpty()) {
                Entry<Pair<Box, Goal>> actionEntry = actions.dequeueMin();

                double cost = actionEntry.getPriority();
                Pair<Box, Goal> action = actionEntry.getValue();
                if (action == null) continue;

                Goal goal = action.getValue();
                Box box = action.getKey();

                System.err.println(box + " " + goal + " " + agent + " " + cost);
            }
        }
    }

    private double getActionCost(Agent agent, Box box, Goal goal) {
        double dist = ManhattanDistance(box.getCoordinate(), goal.getCoordinate());
        dist += ManhattanDistance(agent.getCoordinate(), box.getCoordinate());
        return dist;
    }
}
