package board;

public class Wall extends SokobanObject {

    public Wall(int id, int row, int col) {
        super(id, row, col);
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
