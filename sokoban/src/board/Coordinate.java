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

    public boolean isNeighbour(Coordinate other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col) == 1;
    }

    public static int manhattanDistance(Coordinate c1, Coordinate c2) {
        return Math.abs(c1.getRow() - c2.getRow()) + Math.abs(c1.getCol() - c2.getCol());
    }

    public static Set<Coordinate> getEmptyCellsWithFixedDistanceFrom(Coordinate from, int distance) {
        Set<Coordinate> emptyCells = ClientManager.getInstance().getLevelManager().getLevel().getEmptyCellsPositions();
        return emptyCells.stream().filter(to -> manhattanDistance(from, to) == distance).collect(Collectors.toSet());
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

    /**
     * An edge cell is a cell surrounded by at least 3 objects among walls and solved goals
     * Don't consider goal cells!
     *
     * @param coordinate cell to be examined
     * @return true if edge cell, false otherwise
     */
    public static boolean isEdgeCell(Coordinate coordinate, boolean excludeGoals) {
        if (excludeGoals && Level.isGoalCell(coordinate))
            return false;
        List<Coordinate> neighbours = coordinate.getClockwiseNeighbours();
        Level level = ClientManager.getInstance().getLevelManager().getLevel();
        int validNeighbours = 0;
        for (Coordinate neighbour : neighbours) {
            if (!Level.isNotWall(neighbour)) // Is wall
                validNeighbours++;
            SokobanObject object = level.dynamicObjectAt(neighbour);
            if (object instanceof Box) {
                if (!Level.isGoalCell(neighbour))
                    validNeighbours++; // Is box on a non-goal cell
            }
            //if (Level.isGoalCell(neighbour) && level.dynamicObjectAt(neighbour) == null) // Empty goal cell
            //    validNeighbours--; // Avoid cells close to unsolved goals

        }
        return validNeighbours == 3; // If 4, cell is unreachable!
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
