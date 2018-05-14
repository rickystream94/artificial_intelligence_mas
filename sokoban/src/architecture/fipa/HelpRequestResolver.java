package architecture.fipa;

import architecture.agent.AgentThread;
import architecture.agent.LockDetector;
import board.Agent;
import board.AgentStatus;
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
        if (request == null)
            return;
        if (request.doneHelpingCaller()) {
            // Caller is no more stuck, we can safely discard the help request
            this.helpRequests.remove();
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: thanks to my help, agent %c is no more stuck.", this.agent.getAgentId(), request.getCaller().getAgent().getAgentId()));
        } else {
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: agent %c is still stuck, keep helping him!", this.agent.getAgentId(), request.getCaller().getAgent().getAgentId()));
            lockDetector.addBlockingObject(request.getBlockingObject());
        }
    }

    public void addHelpRequest(HelpRequest helpRequest) {
        this.helpRequests.add(helpRequest);
    }

    /**
     * Sets agent's status to stuck, and asks for help if not already asked previously.
     *
     * @param caller
     * @param ex
     */
    public void askForHelp(AgentThread caller, Exception ex) {
        // If the agent is not already stuck ask for help
        if (caller.getAgent().getStatus() != AgentStatus.STUCK) {
            ConsoleLogger.logInfo(LOGGER, ex.getMessage());
            caller.getAgent().setStatus(AgentStatus.STUCK);

            // Create help request dynamically
            HelpRequest helpRequest = null;
            if (ex instanceof StuckByForeignBoxException)
                helpRequest = new ClearBoxRequest(caller, ((StuckByForeignBoxException) ex).getBox());
            else if (ex instanceof StuckByAgentException) {
                StuckByAgentException exception = (StuckByAgentException) ex;
                if (exception.getBlockingAgent().getStatus() == AgentStatus.FREE)
                    helpRequest = new ClearCellRequest(caller, ((StuckByAgentException) ex).getBlockingAgent());
                else {
                    ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: CONFLICT with agent %c!", this.agent.getAgentId(), exception.getBlockingAgent().getAgentId()));
                    // TODO: conflict! agent with least priority could send a clear cell request to the blocking agent
                    return;
                }
            }

            // Dispatch help request on the Bus
            PerformativeManager.getDefault().dispatchPerformative(helpRequest);
        } else {
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Waiting for help...", this.agent.getAgentId()));
        }
    }
}
