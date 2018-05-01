package board;

import java.util.Objects;

public class Wall extends SokobanObject {

    public Wall(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Wall)) {
            return false;
        }
        Wall other = (Wall) o;
        return Objects.equals(super.getCoordinate(), other.getCoordinate());
    }
}
