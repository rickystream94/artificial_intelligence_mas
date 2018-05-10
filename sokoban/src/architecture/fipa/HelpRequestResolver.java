package architecture.fipa;

import architecture.agent.AgentThread;
import architecture.agent.AgentThreadStatus;
import architecture.agent.LockDetector;
import board.Agent;
import exceptions.StuckByAgentException;
import exceptions.StuckByForeignBoxException;
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

                if (request instanceof ClearBoxRequest)
                    lockDetector.addBlockingBox(((ClearBoxRequest) request).getBlockingBox());
            }
        }
    }

    public void addHelpRequest(HelpRequest helpRequest) {
        this.helpRequests.add(helpRequest);
    }

    public void askForHelp(AgentThread agentThread, Exception ex) {
        // If the agent is not already stuck ask for help
        if (agentThread.getStatus() != AgentThreadStatus.STUCK) {
            ConsoleLogger.logInfo(LOGGER, ex.getMessage());
            // Create the message and dispatch it on the Bus
            agentThread.setStatus(AgentThreadStatus.STUCK);

            // Create help request dynamically
            HelpRequest helpRequest = null;
            if (ex instanceof StuckByForeignBoxException)
                helpRequest = new ClearBoxRequest(((StuckByForeignBoxException) ex).getBox(), agentThread);
            else if (ex instanceof StuckByAgentException) {
                // TODO: not enough, should use a more sophisticated messaging system with acknowledgements
                // should check on status of agent threads: if both are WORKING --> conflict!
                // if only one is working, ask for help
                helpRequest = new ClearCellRequest(((StuckByAgentException) ex).getBlockingAgent(), agentThread);
            }

            PerformativeManager.getDefault().dispatchPerformative(helpRequest);
        } else {
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Waiting for help...", this.agent.getAgentId()));
        }
    }
}
