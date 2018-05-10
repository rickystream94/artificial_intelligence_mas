package architecture.agent;

import board.Box;

public class HelpWithBoxRequest {

    private Box blockingBox;
    private AgentThread caller;

    public HelpWithBoxRequest(Box blockingBox, AgentThread caller) {
        this.blockingBox = blockingBox;
        this.caller = caller;
    }

    public boolean doneHelpingCaller() {
        return this.caller.getStatus() != AgentThreadStatus.STUCK;
    }

    public Box getBlockingBox() {
        return blockingBox;
    }

    public AgentThread getCaller() {
        return this.caller;
    }
}
