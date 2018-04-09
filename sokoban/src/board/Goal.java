package board;

public class Goal extends SokobanObject {

    private char goalType;

    public Goal(int row, int col, char goalType, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.goalType = goalType;
    }

    public char getGoalType() {
        return this.goalType;
    }
}
