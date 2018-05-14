package architecture.fipa;

import architecture.agent.AgentThread;
import architecture.agent.LockDetector;
import architecture.conflicts.Conflict;
import architecture.conflicts.ConflictResponse;
import architecture.conflicts.ConflictResponseGatherer;
import architecture.conflicts.GlobalConflictResolver;
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
        if (!this.helpRequests.contains(helpRequest)) // Avoid inserting duplicates
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
                    helpRequest = new ClearCellRequest(caller, exception.getBlockingAgent());
                else {
                    ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: CONFLICT with agent %c! Wait for global resolver...", this.agent.getAgentId(), exception.getBlockingAgent().getAgentId()));

                    // Send conflict to centralized component and wait for reply
                    GlobalConflictResolver.getInstance().registerConflict(new Conflict(caller, exception.getBlockingAgent()));
                    ConflictResponseGatherer conflictResponseGatherer = caller.getConflictResponseGatherer();
                    ConflictResponse conflictResponse = conflictResponseGatherer.waitForConflictResponse();

                    // Process reply: master will still be stuck, slave will be asked to free the cell --> WORKING!
                    String message;
                    if (!conflictResponse.isMaster()) {
                        message = "Agent %c: I am the SLAVE helping agent %c";
                        caller.getAgent().setStatus(AgentStatus.WORKING);
                    } else
                        message = "Agent %c: I am the MASTER being helped by agent %c";
                    ConsoleLogger.logInfo(LOGGER, String.format(message, agent.getAgentId(), exception.getBlockingAgent().getAgentId()));
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
