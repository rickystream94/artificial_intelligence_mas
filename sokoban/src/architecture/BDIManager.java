package architecture;

import board.*;
import planning.actions.CompoundTask;
import server.action.Action;
import utils.FibonacciHeap;
import utils.FibonacciHeap.Entry;
import javafx.util.Pair;

import java.util.*;

public class BDIManager {

    private Level level;
    private AbstractMap<Agent, FibonacciHeap<Pair<Box, Goal>>> actionsByAgent;

    public BDIManager(Level level) {
        this.level = level;
        this.actionsByAgent = new HashMap<>();
    }

    public void GenerateActionsByAgent() {
        HashSet<Agent> agents = new HashSet<>(this.level.getAgents());
        HashSet<Goal> goals = new HashSet<>(this.level.getGoals());
        HashSet<Box> boxes = new HashSet<>(this.level.getBoxes());
        AbstractMap<Agent,List<Box>> agentsAndBoxMap = new HashMap<>();
        AbstractMap<Box,List<Goal>> boxesAndGoalsMap = new HashMap<>();
        AbstractMap<Pair<Box, Goal>,Pair<Agent, Double>> actionsByAgent = new HashMap<>();

        for (Agent agent : agents) {
            for (Box box : boxes) {
                if(box.getColor() != agent.getColor()) continue;
                List<Box> boxesForAgent = agentsAndBoxMap.get(agent);
                if(boxesForAgent == null) {
                    boxesForAgent = new ArrayList<>();
                    agentsAndBoxMap.put(agent,boxesForAgent);
                }
                boxesForAgent.add(box);
            }
        }

        for (Box box : boxes) {
            for (Goal goal : goals) {
                if(Character.toLowerCase(box.getBoxType()) != goal.getGoalType()) continue;
                List<Goal> goalsForBox = boxesAndGoalsMap.get(box);
                if(goalsForBox == null) {
                    goalsForBox = new ArrayList<>();
                    boxesAndGoalsMap.put(box,goalsForBox);
                }
                goalsForBox.add(goal);
            }
        }

        for(Map.Entry<Agent,List<Box>> entry : agentsAndBoxMap.entrySet()) {
            Agent agent = entry.getKey();
            List<Box> boxesForAgent = entry.getValue();
            for (Box box: boxesForAgent) {
                List<Goal> goalsForBox = boxesAndGoalsMap.get(box);
                if(goalsForBox == null) continue;
                for (Goal goal : goalsForBox) {
                    double dist = getActionCost(agent, box, goal);

                    Pair<Box, Goal> action = new Pair<>(box, goal);
                    Pair<Agent, Double> responsible = actionsByAgent.get(action);

                    if(responsible == null) {
                        actionsByAgent.put(action,new Pair<>(agent, dist));
                    }
                    else {
                        double cost = responsible.getValue();
                        if(cost > dist) {
                            actionsByAgent.put(action,new Pair<>(agent, dist));
                        }
                    }
                }
            }
        }

        for (Map.Entry<Pair<Box, Goal>,Pair<Agent, Double>> entry: actionsByAgent.entrySet()){
            Agent agent = entry.getValue().getKey();
            Double cost = entry.getValue().getValue();
            FibonacciHeap<Pair<Box, Goal>> actions = this.actionsByAgent.get(agent);
            if(actions == null) {
                actions = new FibonacciHeap<>();
                this.actionsByAgent.put(agent,actions);
            }
            actions.enqueue(entry.getKey(),cost);
        }
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
     *
     * This function fucks up the FibonacciHeap
     *
     */
    private void printActions() {
        for(Map.Entry<Agent, FibonacciHeap<Pair<Box, Goal>>> entry : this.actionsByAgent.entrySet()) {
            Agent agent = entry.getKey();
            FibonacciHeap<Pair<Box, Goal>> actions = entry.getValue();

            while(!actions.isEmpty()) {
                Entry<Pair<Box, Goal>> actionEntry = actions.dequeueMin();

                double cost = actionEntry.getPriority();
                Pair<Box, Goal> action = actionEntry.getValue();
                if(action == null) continue;

                Goal goal = action.getValue();
                Box box = action.getKey();

                System.err.println(box + " " + goal + " " + agent + " " + cost);
            }
        }
    }

    private double getActionCost(Agent agent, Box box, Goal goal) {
        double dist = ManhattanDistance(box.getCoordinate(),goal.getCoordinate());
        dist += ManhattanDistance(agent.getCoordinate(),box.getCoordinate());
        return dist;
    }

    private double ManhattanDistance(Coordinate c0, Coordinate c1) {
        return Math.abs(c1.getCol() - c0.getCol()) + Math.abs(c1.getRow() - c0.getRow());
    }

}
