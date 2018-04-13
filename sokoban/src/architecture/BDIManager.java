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
        actionsByAgent = new HashMap<>();
    }

    public void GenerateActionsByAgent() {
        HashSet<Agent> agents = new HashSet<>(level.getAgents());
        HashSet<Goal> goals = new HashSet<>(level.getGoals());
        HashSet<Box> boxes = new HashSet<>(level.getBoxes());
        AbstractMap<Agent,List<Box>> agentsAndBoxMap = new HashMap<>();
        AbstractMap<Box,List<Goal>> boxesAndGoalsMap = new HashMap<>();
        AbstractMap<Agent, FibonacciHeap<Pair<Box, Goal>>> actionsByAgent = new HashMap<>();

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
                    double dist = ManhattanDistance(box.getCoordinate(),goal.getCoordinate());
                    dist += ManhattanDistance(agent.getCoordinate(),box.getCoordinate());
                    FibonacciHeap<Pair<Box, Goal>> actionsByAgentHeap = actionsByAgent.get(agent);
                    if(actionsByAgentHeap == null) {
                        actionsByAgentHeap = new FibonacciHeap<>();
                        this.actionsByAgent.put(agent,actionsByAgentHeap);
                    }
                    actionsByAgentHeap.enqueue(new Pair<>(box, goal), dist);
                }
            }
        }

        for(Map.Entry<Agent, FibonacciHeap<Pair<Box, Goal>>> entry : actionsByAgent.entrySet()) {
            Agent agent = entry.getKey();
            FibonacciHeap<Pair<Box, Goal>> actionsByAgentHeap = entry.getValue();
            Entry<Pair<Box, Goal>> actionEntry = actionsByAgentHeap.dequeueMin();
            if (actionEntry == null) continue;
            Pair<Box, Goal> action = actionEntry.getValue();
            for (Map.Entry<Agent, FibonacciHeap<Pair<Box, Goal>>> entryOther : actionsByAgent.entrySet()) {
                Agent agentOther = entryOther.getKey();
                FibonacciHeap<Pair<Box, Goal>> actionsByAgentHeapOther = entryOther.getValue();
                if(agent.equals(agentOther)) continue;

                FibonacciHeap<Pair<Box, Goal>> actionsByAgentHeapOtherCopy = new FibonacciHeap<>();

                while(true) {
                    Entry<Pair<Box, Goal>> actionEntryOther = actionsByAgentHeapOther.dequeueMin();
                    if (actionEntryOther == null) break;
                    Pair<Box, Goal> actionOther = actionEntryOther.getValue();
                    if(actionOther.equals(action)) {

                    }

                    actionsByAgentHeapOtherCopy.enqueue(actionEntryOther.getValue(),actionEntryOther.getPriority());
                }
            }
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

    //TODO Create a Heuristic Class
    public double ManhattanDistance(Coordinate c0, Coordinate c1) {
        return Math.abs(c1.getCol() - c0.getCol()) + Math.abs(c1.getRow() - c0.getRow());
    }

}
