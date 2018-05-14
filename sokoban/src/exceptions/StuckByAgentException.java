package exceptions;

import board.Agent;

public class StuckByAgentException extends Exception {

    private Agent agent;
    private Agent blockingAgent;

    public StuckByAgentException(Agent agent, Agent blockingAgent) {
        this.agent = agent;
        this.blockingAgent = blockingAgent;
    }

    public Agent getAgent() {
        return agent;
    }

    public Agent getBlockingAgent() {
        return blockingAgent;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c is hindered by agent %c, please clear the way!", agent.getAgentId(), blockingAgent.getAgentId());
    }
}
