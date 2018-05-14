package architecture.bdi;

import board.Agent;
import board.Box;
import board.Coordinate;
import planning.actions.GoToLocationTask;

import java.util.Objects;

public class ClearCellDesire implements Desire {

    private Agent agent;
    private Coordinate target;

    public ClearCellDesire(Agent agent, Coordinate target) {
        this.agent = agent;
        this.target = target;
    }

    public Agent getAgent() {
        return agent;
    }

    @Override
    public Box getBox() {
        return null;
    }

    @Override
    public Coordinate getTarget() {
        return this.target;
    }

    @Override
    public Intention getIntention() {
        return new Intention(new GoToLocationTask(target));
    }

    @Override
    public String toString() {
        return "ClearCell{" + this.agent.getCoordinate() + " --> " + this.target + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClearCellDesire)) return false;
        ClearCellDesire clearCellDesire = (ClearCellDesire) o;
        return target.equals(clearCellDesire.target) && agent.equals(clearCellDesire.agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agent, target);
    }
}
