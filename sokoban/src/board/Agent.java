package board;

import java.util.Objects;

public class Agent extends SokobanObject {

    private char agentId;
    private Color color;
    private Box currentTargetBox;

    public Agent(int row, int col, char agentId, Color color, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.agentId = agentId;
        this.color = color;
    }

    /**
     * Copy constructor
     *
     * @param agent other agent to copy
     */
    public Agent(Agent agent) {
        super(agent);
        this.agentId = agent.agentId;
        this.color = agent.color;
    }

    public Color getColor() {
        return this.color;
    }

    public char getAgentId() {
        return this.agentId;
    }

    public Box getCurrentTargetBox() {
        return currentTargetBox;
    }

    public void setCurrentTargetBox(Box currentTargetBox) {
        this.currentTargetBox = currentTargetBox;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if (!(o instanceof Agent)) return false;
        Agent agent = (Agent) o;
        return agentId == agent.agentId &&
                color == agent.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), agentId, color);
    }

    @Override
    public String toString() {
        return "Agent{" +
                "agentId=" + agentId +
                ", color=" + color +
                '}';
    }
}
