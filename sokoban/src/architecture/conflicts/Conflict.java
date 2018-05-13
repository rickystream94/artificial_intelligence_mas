package architecture.conflicts;

import architecture.agent.AgentThread;
import board.Agent;

public class Conflict {

    private Agent blockingAgent;
    private AgentThread caller;

    public Conflict(AgentThread caller, Agent blockingAgent) {
        this.blockingAgent = blockingAgent;
        this.caller = caller;
    }

    public Agent getBlockingAgent() {
        return blockingAgent;
    }

    public AgentThread getCaller() {
        return caller;
    }
}
