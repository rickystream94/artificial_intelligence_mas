package board;

public class Agent extends SokobanObject {

    private char agentType;
    private String color;

    public Agent(int row, int col, char agentType, String color, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.agentType = agentType;
        this.color = color;
    }

    public String getColor() {
        return this.color;
    }

    public char getAgentType() {
        return this.agentType;
    }
}
