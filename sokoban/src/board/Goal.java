package board;

import java.util.Objects;

public class Goal extends SokobanObject {

    private char goalType;

    public Goal(int row, int col, char goalType, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.goalType = goalType;
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
        return "Goal{" +
                "goalType=" + goalType +
                '}';
    }
}
