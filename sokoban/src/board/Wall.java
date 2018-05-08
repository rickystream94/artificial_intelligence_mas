package board;

public class Wall extends SokobanObject {

    public Wall(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!super.equals(o)) return false;
        return o instanceof Wall;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
