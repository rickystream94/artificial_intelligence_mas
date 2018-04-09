package board;

import java.util.Objects;

public class EmptyCell extends SokobanObject {

    public EmptyCell(int row, int col, SokobanObjectType type) {
        super(row, col, type);
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof EmptyCell)) {
            return false;
        }
        EmptyCell other = (EmptyCell) o;
        return Objects.equals(super.getCoordinate(), other.getCoordinate());
    }
}
