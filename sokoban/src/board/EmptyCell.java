package board;

public class EmptyCell extends SokobanObject {

    public EmptyCell(int row, int col) {
        super(row, col);
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
