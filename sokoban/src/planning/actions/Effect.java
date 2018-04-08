package planning.actions;

import board.Coordinate;

public class Effect {
    private Coordinate newAgentPosition;
    private Coordinate newBoxPosition;

    /**
     * Only agent moves
     *
     * @param newAgentPosition new agent's position
     */
    public Effect(Coordinate newAgentPosition) {
        this.newAgentPosition = newAgentPosition;
        this.newBoxPosition = null;
    }

    /**
     * Both agent and box move
     *
     * @param newAgentPosition new agent's position
     * @param newBoxPosition   new box position
     */
    public Effect(Coordinate newAgentPosition, Coordinate newBoxPosition) {
        this.newAgentPosition = newAgentPosition;
        this.newBoxPosition = newBoxPosition;
    }

    public Coordinate getNewAgentPosition() {
        return newAgentPosition;
    }

    public Coordinate getNewBoxPosition() {
        return newBoxPosition;
    }
}
