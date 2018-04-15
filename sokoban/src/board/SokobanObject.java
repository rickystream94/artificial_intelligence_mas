package board;

import java.util.Objects;

public abstract class SokobanObject {

    private SokobanObjectType objectType;
    private Coordinate coordinate;

    public SokobanObject(int row, int col, SokobanObjectType objectType) {
        this.coordinate = new Coordinate(row, col);
        this.objectType = objectType;
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

    public String getObjectType() {
        return objectType.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getCoordinate());
    }
}
