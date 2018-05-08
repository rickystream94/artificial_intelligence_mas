package architecture.fipa;

import architecture.agent.AgentThread;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.List;
import java.util.logging.Logger;

public abstract class Performative {
    private PerformativeType performativeType ;
    private AgentThread asker;
    protected static final Logger LOGGER = ConsoleLogger.getLogger(Performative.class.getSimpleName());

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
