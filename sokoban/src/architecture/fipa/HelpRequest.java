package architecture.fipa;

import architecture.agent.AgentThread;
import architecture.agent.AgentThreadStatus;
import utils.FibonacciHeap;

import java.util.List;

public abstract class HelpRequest {

    protected AgentThread caller;

    protected HelpRequest(AgentThread caller) {
        this.caller = caller;
    }

    protected abstract void chooseHelper(AgentThread helper);

    protected abstract FibonacciHeap<AgentThread> findBestHelpers(List<AgentThread> agentThreadHelpers, AgentThread agentThread);

    protected AgentThread getCaller() {
        return caller;
    }

    protected boolean doneHelpingCaller() {
        return this.caller.getStatus() != AgentThreadStatus.STUCK;
    }

    protected int helperPriorityByStatus(AgentThread helper) {
        switch (helper.getStatus()) {
            case FREE:
                return -100;
            case WORKING:
                return 0;
            case STUCK:
                return 100;
            default:
                return 0;
        }
    }
}
