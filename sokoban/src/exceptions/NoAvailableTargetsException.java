package exceptions;

import board.Agent;
import board.SokobanObject;

public class NoAvailableTargetsException extends Exception {

    private Agent agent;
    private SokobanObject blockingObject;

    public NoAvailableTargetsException(Agent agent, SokobanObject blockingObject) {
        this.agent = agent;
        this.blockingObject = blockingObject;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: no more available targets to choose for blocking object %s!", agent.getAgentId(), blockingObject);
    }

    public SokobanObject getBlockingObject() {
        return this.blockingObject;
    }
}
