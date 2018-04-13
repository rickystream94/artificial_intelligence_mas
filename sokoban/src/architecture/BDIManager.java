package architecture;

import board.Agent;
import board.Coordinate;
import board.Level;
import planning.actions.CompoundTask;
import server.action.Action;
import utils.FibonacciHeap;
import utils.FibonacciHeap.Entry;


import java.util.AbstractMap;
import java.util.HashMap;

public class BDIManager {

    private Level level;
    private AbstractMap<Agent, FibonacciHeap<CompoundTask>> actionsByAgent;

    public BDIManager(Level level) {
        this.level = level;
        actionsByAgent = new HashMap<>();
    }

    public CompoundTask getCompoundTaskByAgent(Agent agent) {
        FibonacciHeap<CompoundTask> compoundTaskHeap = actionsByAgent.get(agent);

        if(compoundTaskHeap != null) {
            Entry<CompoundTask> compoundTask = compoundTaskHeap.dequeueMin();
            if(compoundTask != null)
                return compoundTask.getValue();
        }
        
        return null;
    }

    //TODO Create a Heuristic Class
    public int ManhattanDistance(Coordinate c0, Coordinate c1) {
        return Math.abs(c0.getCol() - c0.getCol()) + Math.abs(c0.getRow() - c0.getRow());
    }

}
