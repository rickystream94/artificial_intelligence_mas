package exceptions;

public class InvalidActionException extends Exception {

    private final char agentId;

    public InvalidActionException(char agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: Last action was not accepted by server!", agentId);
    }
}
