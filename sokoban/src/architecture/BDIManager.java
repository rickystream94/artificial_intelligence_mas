package architecture;

import board.*;
import planning.actions.CompoundTask;
import server.action.Action;
import utils.FibonacciHeap;
import utils.FibonacciHeap.Entry;
import javafx.util.Pair;

import java.util.*;

public class BDIManager {

    private AbstractMap<Agent, FibonacciHeap<Pair<Box, Goal>>> actionsByAgent;

    public BDIManager() {
        this.actionsByAgent = new HashMap<>();
    }

    public void generateActionsByAgent() {
        Level level = ClientManager.getInstance().getLevelManager().getLevel();

        HashSet<Agent> agents = new HashSet<>(level.getAgents());
        HashSet<Goal> goals = new HashSet<>(level.getGoals());
        HashSet<Box> boxes = new HashSet<>(level.getBoxes());
        AbstractMap<Agent,List<Box>> agentAndBoxesMap = new HashMap<>();
        AbstractMap<Box,List<Goal>> boxAndGoalsMap = new HashMap<>();
        AbstractMap<Pair<Box, Goal>,Pair<Agent, Double>> actionsByAgent = new HashMap<>();

        for (Agent agent : agents) {
            for (Box box : boxes) {
                if(box.getColor() != agent.getColor()) continue;
                if(!agentAndBoxesMap.containsKey(agent))
                    agentAndBoxesMap.put(agent,new ArrayList<>());
                agentAndBoxesMap.get(agent).add(box);
            }
        }

        for (Box box : boxes) {
            for (Goal goal : goals) {
                if(Character.toLowerCase(box.getBoxType()) != goal.getGoalType()) continue;
                if(!boxAndGoalsMap.containsKey(box))
                    boxAndGoalsMap.put(box,new ArrayList<>());
                boxAndGoalsMap.get(box).add(goal);
            }
        }

        for(Map.Entry<Agent,List<Box>> entry : agentAndBoxesMap.entrySet()) {
            Agent agent = entry.getKey();
            List<Box> boxesForAgent = entry.getValue();
            for (Box box: boxesForAgent) {
                if(!boxAndGoalsMap.containsKey(box)) continue;
                List<Goal> goalsForBox = boxAndGoalsMap.get(box);
                for (Goal goal : goalsForBox) {
                    double dist = getActionCost(agent, box, goal);
                    Pair<Box, Goal> action = new Pair<>(box, goal);
                    if(!actionsByAgent.containsKey(action)) {
                        actionsByAgent.put(action,new Pair<>(agent, dist));
                    }
                    else {
                        Pair<Agent, Double> responsible = actionsByAgent.get(action);
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
            if(!this.actionsByAgent.containsKey(agent))
                this.actionsByAgent.put(agent, new FibonacciHeap<>());
            FibonacciHeap<Pair<Box, Goal>> actions = this.actionsByAgent.get(agent);
            actions.enqueue(entry.getKey(), cost);
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
