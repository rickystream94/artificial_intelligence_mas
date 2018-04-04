package board;

public class Agent extends SokobanObject {

    private char agentType;
    private Color color;

    public Agent(int row, int col, char agentType, Color color, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.agentType = agentType;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public char getAgentType() {
        return this.agentType;
    }
}
