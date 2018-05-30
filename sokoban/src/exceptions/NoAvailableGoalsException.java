package exceptions;

import board.Agent;

public class NoAvailableGoalsException extends Exception {

    private Agent agent;

    public NoAvailableGoalsException(Agent agent) {
        this.agent = agent;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: No solvable goals for me", agent.getAgentId());
    }
}
