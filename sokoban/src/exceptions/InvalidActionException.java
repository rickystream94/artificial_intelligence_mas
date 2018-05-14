package exceptions;

import planning.actions.PrimitiveTask;

public class InvalidActionException extends Exception {

    private final char agentId;
    private PrimitiveTask failedAction;

    public InvalidActionException(char agentId, PrimitiveTask failedAction) {
        this.agentId = agentId;
        this.failedAction = failedAction;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: Max number of retries for action %s. Detecting issue...", agentId, failedAction);
    }

    public PrimitiveTask getFailedAction() {
        return failedAction;
    }
}
