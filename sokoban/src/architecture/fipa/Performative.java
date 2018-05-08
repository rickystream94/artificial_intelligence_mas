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

    protected int helperPriorityByStatus(AgentThread helper) {
        switch (helper.getStatus()) {
            case FREE:
                return -100;
            case WORKING:
                return 100;
            default:
                return 0;
        }
    }

    public abstract void execute(AgentThread helper);

    public abstract FibonacciHeap<AgentThread> findBests(List<AgentThread> agentThreadHelpers, AgentThread agentThread);
}
