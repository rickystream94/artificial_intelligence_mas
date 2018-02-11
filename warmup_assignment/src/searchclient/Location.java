package searchclient;

public class Location {
    private int row;
    private int col;

    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Location l = (Location) obj;
        return l.getRow() == this.row && l.getCol() == this.col;
    }

    @Override
    public int hashCode() {
        return this.row + this.col;
    }

    @Override
    public String toString() {
        return "(" + this.row + "," + this.col + ")";
    }
}