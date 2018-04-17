package board;

import java.util.Objects;

public class Box extends SokobanObject {

    private char boxType;
    private Color color;

    public Box(int row, int col, char boxType, Color color, SokobanObjectType objectType) {
        super(row, col, objectType);
        this.boxType = boxType;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public char getBoxType() {
        return this.boxType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box box = (Box) o;
        return super.equals(o) &&
                boxType == box.boxType &&
                color == box.color;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), boxType, color);
    }

    @Override
    public String toString() {
        return "Box{" +
                "boxType=" + boxType +
                ", color=" + color +
                '}';
    }
}
