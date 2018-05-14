package planning.relaxations;

import board.Coordinate;

/**
 * Considers all boards objects except of agents
 */
public class NoAgentsRelaxation extends Relaxation {

    protected NoAgentsRelaxation() {
    }

    private boolean agentAt(Coordinate coordinate) {
        return this.levelManager.getLevel().getAgentsMap().containsKey(coordinate);
    }

    @Override
    public boolean movePreconditionsMet(Coordinate targetPosition) {
        return agentAt(targetPosition) || isEmpty(targetPosition);
    }

    @Override
    public boolean pushPreconditionsMet(Coordinate agentTargetPosition, Coordinate currentBoxPosition, Coordinate boxTargetPosition) {
        return (agentAt(boxTargetPosition) || isEmpty(boxTargetPosition)) && positionsCoincide(currentBoxPosition, agentTargetPosition);
    }

    @Override
    public boolean pullPreconditionsMet(Coordinate currentAgentPosition, Coordinate agentTargetPosition, Coordinate boxTargetPosition) {
        return (agentAt(agentTargetPosition) || isEmpty(agentTargetPosition)) && positionsCoincide(currentAgentPosition, boxTargetPosition);
    }
}
