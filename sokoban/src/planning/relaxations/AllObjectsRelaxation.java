package planning.relaxations;

import architecture.LevelManager;
import board.Coordinate;

/**
 * Considers all boards objects (ideal for SA environment)
 */
public class AllObjectsRelaxation extends Relaxation {

    protected AllObjectsRelaxation(LevelManager levelManager) {
        super(levelManager);
    }

    @Override
    public boolean movePreconditionsMet(Coordinate targetPosition) {
        return isEmpty(targetPosition);
    }

    @Override
    public boolean pushPreconditionsMet(Coordinate agentTargetPosition, Coordinate currentBoxPosition, Coordinate boxTargetPosition) {
        return isEmpty(boxTargetPosition) && positionsCoincide(currentBoxPosition, agentTargetPosition);
    }

    @Override
    public boolean pullPreconditionsMet(Coordinate currentAgentPosition, Coordinate agentTargetPosition, Coordinate boxTargetPosition) {
        return isEmpty(agentTargetPosition) && positionsCoincide(currentAgentPosition, boxTargetPosition);
    }
}
