package architecture.agent;

import board.Box;

public class HelpRequestResolver {

    private Box blockingBox;
    private AgentThread caller;

    public HelpRequestResolver(Box blockingBox, AgentThread caller) {
        this.blockingBox = blockingBox;
        this.caller = caller;
    }

    public boolean doneHelpingCaller() {
        return this.caller.getStatus() != AgentThreadStatus.STUCK;
    }
}
