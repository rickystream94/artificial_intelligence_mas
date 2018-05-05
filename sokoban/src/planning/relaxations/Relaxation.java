package planning.relaxations;

import architecture.ClientManager;
import architecture.LevelManager;
import board.Coordinate;

public abstract class Relaxation {

    protected LevelManager levelManager;

    protected Relaxation() {
        this.levelManager = ClientManager.getInstance().getLevelManager();
    }

    protected boolean positionsCoincide(Coordinate pos1, Coordinate pos2) {
        return pos1.equals(pos2);
    }

    protected boolean isEmpty(Coordinate objectPosition) {
        return this.levelManager.getLevel().isCellEmpty(objectPosition) || this.levelManager.getLevel().getAgentsMap().containsKey(objectPosition);
    }

    public abstract boolean movePreconditionsMet(Coordinate targetPosition);

    public abstract boolean pushPreconditionsMet(Coordinate agentTargetPosition, Coordinate currentBoxPosition, Coordinate boxTargetPosition);

    public abstract boolean pullPreconditionsMet(Coordinate currentAgentPosition, Coordinate agentTargetPosition, Coordinate boxTargetPosition);
}
