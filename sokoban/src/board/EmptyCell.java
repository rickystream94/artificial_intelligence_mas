package board;

import java.util.Objects;

public class EmptyCell extends SokobanObject {

    public EmptyCell(int row, int col) {
        super(row, col);
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
