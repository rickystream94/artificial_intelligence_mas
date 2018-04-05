package planning;

import board.Agent;

public abstract class AbstractAction {
    private char agentId;

    public AbstractAction(Agent agent) {
        this.agentId = agent.getAgentId();
    }

    public char getAgentId() {
        return this.agentId;
    }
}
