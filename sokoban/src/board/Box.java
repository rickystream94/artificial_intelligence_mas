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

    /**
     * Copy constructor
     *
     * @param box other box to copy
     */
    public Box(Box box) {
        super(box);
        this.boxType = box.boxType;
        this.color = box.color;
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
        if (!super.equals(o)) return false;
        if (!(o instanceof Box)) return false;
        Box box = (Box) o;
        return this.boxType == box.boxType && this.color == box.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), boxType, color);
    }

    @Override
    public String toString() {
        return String.format("Box{boxType=%s, color=%s, %s}", boxType, color, getCoordinate());
    }
}
