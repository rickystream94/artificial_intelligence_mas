package planning.relaxations;

import board.Color;
import board.Coordinate;

public class ForeignBoxesAndWallsRelaxation extends OnlyWallsRelaxation {

    private final Color myColor;

    protected ForeignBoxesAndWallsRelaxation(Color myColor) {
        this.myColor = myColor;
    }

    @Override
    public boolean movePreconditionsMet(Coordinate targetPosition) {
        boolean isMet = super.movePreconditionsMet(targetPosition);
        return isMet && this.levelManager.getLevel().getBoxes().stream()
                .filter(box -> box.getColor() != this.myColor)
                .noneMatch(box -> box.getCoordinate().equals(targetPosition));
    }

    @Override
    public boolean pushPreconditionsMet(Coordinate agentTargetPosition, Coordinate currentBoxPosition, Coordinate boxTargetPosition) {
        boolean isMet = super.pushPreconditionsMet(agentTargetPosition, currentBoxPosition, boxTargetPosition);
        return isMet && this.levelManager.getLevel().getBoxes().stream()
                .filter(box -> box.getColor() != this.myColor)
                .noneMatch(box -> box.getCoordinate().equals(boxTargetPosition));
    }

    @Override
    public boolean pullPreconditionsMet(Coordinate currentAgentPosition, Coordinate agentTargetPosition, Coordinate boxTargetPosition) {
        boolean isMet = super.pullPreconditionsMet(currentAgentPosition, agentTargetPosition, boxTargetPosition);
        return isMet && this.levelManager.getLevel().getBoxes().stream()
                .filter(box -> box.getColor() != this.myColor)
                .noneMatch(box -> box.getCoordinate().equals(agentTargetPosition));
    }
}
