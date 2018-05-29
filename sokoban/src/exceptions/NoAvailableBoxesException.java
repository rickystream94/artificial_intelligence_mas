package exceptions;

import board.Agent;

public class NoAvailableBoxesException extends Exception {

    private Agent agent;

    public NoAvailableBoxesException(Agent agent) {
        this.agent = agent;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: No boxes available for me. Sending NoOp...", agent.getAgentId());
    }
}
