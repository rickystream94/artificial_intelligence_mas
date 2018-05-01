package board;

import java.util.Objects;

public abstract class SokobanObject {

    private Coordinate coordinate;

    public SokobanObject(int row, int col) {
        this.coordinate = new Coordinate(row, col);
    }

    /**
     * Copy constructor
     *
     * @param other other object to copy
     */
    public SokobanObject(SokobanObject other) {
        this.coordinate = new Coordinate(other.coordinate);
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public void setCoordinate(int row, int col) {
        this.coordinate.setRow(row);
        this.coordinate.setCol(col);
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SokobanObject)) return false;
        SokobanObject that = (SokobanObject) o;
        return Objects.equals(coordinate, that.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate);
    }
}
