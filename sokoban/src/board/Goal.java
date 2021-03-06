package board;

import java.util.Objects;

public class Goal extends SokobanObject {

    private char goalType;
    private int priority;

    public Goal(int row, int col, char goalType) {
        super(row, col);
        this.goalType = goalType;
    }

    /**
     * Copy constructor
     *
     * @param goal other goal to copy
     */
    public Goal(Goal goal) {
        super(goal);
        this.goalType = goal.goalType;
    }

    public char getGoalType() {
        return this.goalType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if (!(o instanceof Goal)) return false;
        Goal goal = (Goal) o;
        return this.goalType == goal.goalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), goalType);
    }

    @Override
    public String toString() {
        return "Goal{" + super.toString() +
                ", goalType=" + goalType + ", " + this.getCoordinate() +
                '}';
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
