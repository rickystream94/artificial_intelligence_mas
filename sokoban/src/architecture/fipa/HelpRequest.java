package architecture.fipa;

import architecture.agent.AgentThread;
import board.AgentStatus;
import board.SokobanObject;

import java.util.List;

public abstract class HelpRequest {

    private AgentThread caller;
    private SokobanObject blockingObject;

    protected HelpRequest(AgentThread caller, SokobanObject blockingObject) {
        this.caller = caller;
        this.blockingObject = blockingObject;
    }

    protected abstract void chooseHelper(AgentThread helper);

    protected abstract AgentThread findBestHelper(List<AgentThread> helpers);

    protected AgentThread getCaller() {
        return caller;
    }

    protected SokobanObject getBlockingObject() {
        return blockingObject;
    }

    protected boolean doneHelpingCaller() {
        return this.caller.getAgent().getStatus() != AgentStatus.STUCK;
    }

    protected int helperPriorityByStatus(AgentThread helper) {
        switch (helper.getAgent().getStatus()) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HelpRequest)) return false;
        HelpRequest helpRequest = (HelpRequest) o;
        return helpRequest.caller.equals(this.caller) && helpRequest.blockingObject.equals(this.blockingObject);
    }
}
