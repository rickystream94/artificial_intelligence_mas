package board;

import architecture.ClientManager;

import java.util.*;
import java.util.stream.Collectors;

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

    public static List<Coordinate> getAllCellsWithFixedDistanceFrom(Coordinate from, int distance) {
        Set<Coordinate> allPlayableCells = ClientManager.getInstance().getLevelManager().getLevel().getAllPlayableCells();
        return allPlayableCells.stream().filter(to -> manhattanDistance(from, to) == distance).collect(Collectors.toList());
    }

    public List<Coordinate> getClockwiseNeighbours() {
        List<Coordinate> neighbours = new ArrayList<>();
        if (row - 1 >= 0) {
            neighbours.add(new Coordinate(row - 1, col));
        }
        if (col + 1 < Level.getWidth()) {
            neighbours.add(new Coordinate(row, col + 1));
        }
        if (row + 1 < Level.getHeight()) {
            neighbours.add(new Coordinate(row + 1, col));
        }
        if (col - 1 >= 0) {
            neighbours.add(new Coordinate(row, col - 1));
        }
        return neighbours;
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
