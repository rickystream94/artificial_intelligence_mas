package architecture.fipa;

import architecture.AgentThread;
import board.Color;
import board.Coordinate;
import logging.ConsoleLogger;
import main.ClientMain;
import utils.FibonacciHeap;

import java.util.*;
import java.util.logging.Logger;

public class PerformativeManager {
    private List<AgentThread> listeners;

    private static PerformativeManager instance;

    private PerformativeManager() { listeners = new LinkedList<>(); }

    public static PerformativeManager getDefault() {
        if (instance == null)
            instance = new PerformativeManager();
        return instance;
    }

    public void register(AgentThread agentThread) {
        listeners.add(agentThread);
    }

    public void execute(Performative performative) {
        FibonacciHeap<AgentThread> helpers = performative.findBests(listeners,performative.getAsker());
        while(!helpers.isEmpty()) {
            AgentThread helper = helpers.dequeueMin().getValue();
            performative.execute(helper);
        }
    }
}
