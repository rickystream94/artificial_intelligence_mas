package board;

import java.util.Objects;

public abstract class SokobanObject {

    private SokobanObjectType objectType;
    private Coordinate coordinate;

    public SokobanObject(int row, int col, SokobanObjectType objectType) {
        this.coordinate = new Coordinate(row, col);
        this.objectType = objectType;
    }

    /**
     * Copy constructor
     *
     * @param other other object to copy
     */
    public SokobanObject(SokobanObject other) {
        this.coordinate = new Coordinate(other.coordinate);
        this.objectType = other.objectType;
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

    public SokobanObjectType getObjectType() {
        return objectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SokobanObject)) return false;
        SokobanObject that = (SokobanObject) o;
        return objectType == that.objectType &&
                Objects.equals(coordinate, that.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, coordinate);
    }
}
