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

        if (o == this) return true;
        if (!(o instanceof Box)) {
            return false;
        }
        Box other = (Box) o;
        return Objects.equals(super.getCoordinate(), other.getCoordinate());
    }
}