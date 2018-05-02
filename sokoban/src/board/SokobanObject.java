package board;

import java.util.Objects;

public abstract class SokobanObject {

    private Coordinate coordinate;
    private int objectId;

    public SokobanObject(int objectId, int row, int col) {
        this.coordinate = new Coordinate(row, col);
        this.objectId = objectId;
    }

    /**
     * Copy constructor
     *
     * @param other other object to copy
     */
    public SokobanObject(SokobanObject other) {
        this.coordinate = new Coordinate(other.coordinate);
        this.objectId = other.objectId;
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SokobanObject)) return false;
        SokobanObject that = (SokobanObject) o;
        return Objects.equals(objectId, that.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }

    @Override
    public String toString() {
        return "ID=" + objectId;
    }
}
