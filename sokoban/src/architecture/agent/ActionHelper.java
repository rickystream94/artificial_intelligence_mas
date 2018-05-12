package architecture.agent;

import architecture.ClientManager;
import board.Agent;
import logging.ConsoleLogger;

import java.util.logging.Logger;

public class ActionHelper {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ActionHelper.class.getSimpleName());
    private static final int MAX_ACTION_RETRIES = ClientManager.getInstance().getNumberOfAgents() == 1 ? 1 : 3;
    private final Agent agent;

    private int numFailedActions;

    public ActionHelper(Agent agent) {
        this.agent = agent;
        this.numFailedActions = 0;
    }

    public void actionFailed() {
        this.numFailedActions++;
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: action failed, attempt %d/%d", agent.getAgentId(), numFailedActions, MAX_ACTION_RETRIES));
    }

    public void resetFailedActions() {
        this.numFailedActions = 0;
    }

    public boolean isStuck() {
        return this.numFailedActions == MAX_ACTION_RETRIES;
    }
}
