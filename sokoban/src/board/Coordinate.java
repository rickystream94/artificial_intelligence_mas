package board;

import java.util.Objects;
import java.util.StringJoiner;

public class Coordinate {
    private int row;
    private int col;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Coordinate(Coordinate other) {
        this.row = other.row;
        this.col = other.col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public boolean isNeighbour(Coordinate other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col) == 1;
    }

    public static int manhattanDistance(Coordinate c1, Coordinate c2) {
        return Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol());
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) o;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    @Override
    public String toString() {
        return new StringJoiner(",", "(", ")").add(String.valueOf(this.row)).add(String.valueOf(this.col)).toString();
    }
}
