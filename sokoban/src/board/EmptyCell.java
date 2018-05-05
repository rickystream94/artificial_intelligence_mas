package board;

public class EmptyCell extends SokobanObject {

    public EmptyCell(int id, int row, int col) {
        super(id, row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!super.equals(o)) return false;
        return o instanceof EmptyCell;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
