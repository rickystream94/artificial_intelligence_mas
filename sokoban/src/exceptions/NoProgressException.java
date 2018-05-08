package exceptions;

import board.Agent;

public class NoProgressException extends Exception {

    private Agent agent;

    public NoProgressException(Agent agent) {
        this.agent = agent;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: DEADLOCK, hence cleaning up and re-prioritizing desires", this.agent.getAgentId());
    }
}
