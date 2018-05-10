package architecture.fipa;

import architecture.agent.AgentThread;
import board.Agent;
import utils.FibonacciHeap;

import java.util.List;

public class ClearCellRequest extends HelpRequest {

    private Agent blockingAgent;

    public ClearCellRequest(Agent blockingAgent, AgentThread caller) {
        super(caller);
        this.blockingAgent = blockingAgent;
    }

    @Override
    protected void chooseHelper(AgentThread helper) {

    }

    @Override
    protected FibonacciHeap<AgentThread> findBestHelpers(List<AgentThread> agentThreadHelpers, AgentThread agentThread) {
        return null;
    }
}
