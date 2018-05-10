package architecture.agent;

import board.Agent;
import logging.ConsoleLogger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Logger;

public class HelpRequestResolver {

    private static final Logger LOGGER = ConsoleLogger.getLogger(HelpRequestResolver.class.getSimpleName());

    private Agent agent;
    private Queue<HelpWithBoxRequest> helpWithBoxRequests;

    public HelpRequestResolver(Agent agent) {
        this.agent = agent;
        this.helpWithBoxRequests = new ArrayDeque<>();
    }

    public boolean hasRequestsToProcess() {
        return !this.helpWithBoxRequests.isEmpty();
    }

    public void processHelpRequest(LockDetector lockDetector) {
        HelpWithBoxRequest request = this.helpWithBoxRequests.peek();
        if (request != null) {
            if (request.doneHelpingCaller()) {
                // Caller is no more stuck, we can safely discard the help request
                this.helpWithBoxRequests.remove();
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: thanks to my help, agent %c is no more stuck.", this.agent.getAgentId(), request.getCaller().getAgent().getAgentId()));
            } else {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: agent %c is still stuck, keep helping him!", this.agent.getAgentId(), request.getCaller().getAgent().getAgentId()));
                lockDetector.addBlockingBox(request.getBlockingBox());
            }
        }
    }

    public void addHelpRequest(HelpWithBoxRequest helpWithBoxRequest) {
        this.helpWithBoxRequests.add(helpWithBoxRequest);
    }
}
