package exceptions;

public class PlanNotFoundException extends Exception {

    private final char agentId;

    public PlanNotFoundException(char agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: No plan could be found to achieve the current desire.", agentId);
    }
}
