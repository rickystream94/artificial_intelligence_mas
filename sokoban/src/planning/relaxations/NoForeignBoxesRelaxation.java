package planning.relaxations;

import board.Color;
import board.Coordinate;

/**
 * Only considers walls and boxes of the same color (doesn't consider other agents and boxes of different color):
 * ideal for MA environment if there are only a few agents of the same color
 */
public class NoForeignBoxesRelaxation extends OnlyWallsRelaxation {

    private final Color myColor;

    protected NoForeignBoxesRelaxation(Color myColor) {
        this.myColor = myColor;
    }

    @Override
    public boolean movePreconditionsMet(Coordinate targetPosition) {
        boolean isMet = super.movePreconditionsMet(targetPosition);
        return isMet && this.levelManager.getLevel().getBoxes().stream()
                .filter(box -> box.getColor() == this.myColor)
                .noneMatch(box -> box.getCoordinate().equals(targetPosition));
    }

    @Override
    public boolean pushPreconditionsMet(Coordinate agentTargetPosition, Coordinate currentBoxPosition, Coordinate boxTargetPosition) {
        boolean isMet = super.pushPreconditionsMet(agentTargetPosition, currentBoxPosition, boxTargetPosition);
        return isMet && this.levelManager.getLevel().getBoxes().stream()
                .filter(box -> box.getColor() == this.myColor)
                .noneMatch(box -> box.getCoordinate().equals(boxTargetPosition));
    }

    @Override
    public boolean pullPreconditionsMet(Coordinate currentAgentPosition, Coordinate agentTargetPosition, Coordinate boxTargetPosition) {
        boolean isMet = super.pullPreconditionsMet(currentAgentPosition, agentTargetPosition, boxTargetPosition);
        return isMet && this.levelManager.getLevel().getBoxes().stream()
                .filter(box -> box.getColor() == this.myColor)
                .noneMatch(box -> box.getCoordinate().equals(agentTargetPosition));
    }
}
