package board;

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
}
