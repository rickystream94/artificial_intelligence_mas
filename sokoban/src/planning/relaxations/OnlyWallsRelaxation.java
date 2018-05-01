package planning.relaxations;

import board.Coordinate;
import board.Level;

/**
 * Only considers the walls (doesn't consider any other box or agent):
 * ideal for MA environment if there are lots of agents of the same color (the chance of having the other boxes moved is higher)
 */
public class OnlyWallsRelaxation extends Relaxation {

    protected OnlyWallsRelaxation() {
    }

    @Override
    public boolean movePreconditionsMet(Coordinate targetPosition) {
        return Level.isNotWall(targetPosition);
    }

    @Override
    public boolean pushPreconditionsMet(Coordinate agentTargetPosition, Coordinate currentBoxPosition, Coordinate boxTargetPosition) {
        return Level.isNotWall(boxTargetPosition) && positionsCoincide(currentBoxPosition, agentTargetPosition);
    }

    @Override
    public boolean pullPreconditionsMet(Coordinate currentAgentPosition, Coordinate agentTargetPosition, Coordinate boxTargetPosition) {
        return Level.isNotWall(agentTargetPosition) && positionsCoincide(currentAgentPosition, boxTargetPosition);
    }
}