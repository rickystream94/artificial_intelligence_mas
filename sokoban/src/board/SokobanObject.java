package board;

import java.util.Objects;

public abstract class SokobanObject {

    private Coordinate coordinate;
    private int objectId;

    public SokobanObject(int row, int col) {
        this.coordinate = new Coordinate(row, col);
        this.objectId = Level.getAvailableObjectId(); // Each call to this method ensures that it will return a unique ID
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

    public int getObjectId() {
        return objectId;
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
