package board;

import java.util.Objects;

public class Agent extends SokobanObject {

    private char agentId;
    private Color color;

    public Agent(int row, int col, char agentId, Color color, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.agentId = agentId;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public char getAgentId() {
        return this.agentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return agentId == agent.agentId &&
                color == agent.color;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), agentId, color);
    }
}
