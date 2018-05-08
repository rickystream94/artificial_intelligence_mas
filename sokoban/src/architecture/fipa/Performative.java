package architecture.fipa;

import architecture.agent.AgentThread;
import utils.FibonacciHeap;

import java.util.List;

public abstract class Performative {

    private AgentThread caller;

    protected Performative(AgentThread caller) {
        this.caller = caller;
    }

    public AgentThread getCaller() {
        return caller;
    }

    public abstract void execute(AgentThread helper);

    public abstract FibonacciHeap<AgentThread> findBests(List<AgentThread> agentThreadHelpers, AgentThread agentThread);
}
