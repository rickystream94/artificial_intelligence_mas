package architecture.bdi;

import board.Box;
import board.Coordinate;
import board.Goal;
import planning.actions.SolveGoalTask;

import java.util.Objects;

public class GoalDesire implements Desire {

    private Box box;
    private Goal goal;

    public GoalDesire(Box box, Goal goal) {
        this.box = box;
        this.goal = goal;
    }

    public Goal getGoal() {
        return goal;
    }

    @Override
    public Box getBox() {
        return this.box;
    }

    @Override
    public Coordinate getTarget() {
        return this.goal.getCoordinate();
    }

    @Override
    public Intention getIntention() {
        return new Intention(new SolveGoalTask());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoalDesire)) return false;
        GoalDesire goalDesire = (GoalDesire) o;
        return box.equals(goalDesire.box) && goal.equals(goalDesire.goal);
    }

    /**
     * Super important: since, at runtime, box will change position, hashCode won't return the same value if we hash both
     * box and goal! During intention generation, everything is still static so it's feasible. Then, just hash the goal
     * (it it still an identifier and for sure each goal is mapped to one and only one box)
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(box, goal);
    }

    @Override
    public String toString() {
        return this.box.toString() +
                " --> " +
                this.goal.toString();
    }
}
