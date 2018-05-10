package architecture.fipa;

import architecture.agent.LockDetector;
import board.Agent;
import logging.ConsoleLogger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Logger;

public class HelpRequestResolver {

    private static final Logger LOGGER = ConsoleLogger.getLogger(HelpRequestResolver.class.getSimpleName());

    private Agent agent;
    private Queue<HelpRequest> helpRequests;

    public HelpRequestResolver(Agent agent) {
        this.agent = agent;
        this.helpRequests = new ArrayDeque<>();
    }

    public boolean hasRequestsToProcess() {
        return !this.helpRequests.isEmpty();
    }

    public void processHelpRequest(LockDetector lockDetector) {
        HelpRequest request = this.helpRequests.peek();
        if (request != null) {
            if (request.doneHelpingCaller()) {
                // Caller is no more stuck, we can safely discard the help request
                this.helpRequests.remove();
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: thanks to my help, agent %c is no more stuck.", this.agent.getAgentId(), request.getCaller().getAgent().getAgentId()));
            } else {
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: agent %c is still stuck, keep helping him!", this.agent.getAgentId(), request.getCaller().getAgent().getAgentId()));

                if (request instanceof HelpWithBoxRequest)
                    lockDetector.addBlockingBox(((HelpWithBoxRequest) request).getBlockingBox());
            }
        }
    }

    public void addHelpRequest(HelpRequest helpRequest) {
        this.helpRequests.add(helpRequest);
    }
}
