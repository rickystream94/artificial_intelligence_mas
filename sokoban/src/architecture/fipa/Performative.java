package architecture.fipa;

import architecture.AgentThread;
import board.Agent;
import board.Coordinate;
import logging.ConsoleLogger;
import main.ClientMain;
import utils.FibonacciHeap;

import java.util.List;
import java.util.logging.Logger;

public abstract class Performative {
    private PerformativeType performativeType ;
    private AgentThread asker;
    protected static final Logger LOGGER = ConsoleLogger.getLogger(ClientMain.class.getSimpleName());

    public PerformativeType getPerformativeType() {
        return performativeType;
    }

    public void setPerformativeType(PerformativeType performativeType) {
        this.performativeType = performativeType;
    }

    public AgentThread getAsker() {
        return asker;
    }

    public void setAsker(AgentThread asker) {
        this.asker = asker;
    }

    public abstract void execute(AgentThread helper);

    public abstract FibonacciHeap<AgentThread> findBests(List<AgentThread> agentThreadHelpers, AgentThread agentThread);
}
