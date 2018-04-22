package planning.actions;

import board.Coordinate;

public enum Direction {
    N, W, E, S;

    public static boolean isOpposite(Direction d1, Direction d2) {
        return d1.ordinal() + d2.ordinal() == 3;
    }

    public static Coordinate getPositionByDirection(Coordinate from, Direction d) {
        switch (d) {
            case N:
                return new Coordinate(from.getRow() - 1, from.getCol());
            case S:
                return new Coordinate(from.getRow() + 1, from.getCol());
            case E:
                return new Coordinate(from.getRow(), from.getCol() + 1);
            case W:
                return new Coordinate(from.getRow(), from.getCol() - 1);
            default:
                return null;
        }
    }

    public static Direction getOpposite(Direction d) {
        switch (d) {
            case N:
                return S;
            case E:
                return W;
            case W:
                return E;
            case S:
                return N;
            default:
                return null;
        }
    }

    public static Direction getDirection(Coordinate from, Coordinate to) {
        if (from.getRow() == to.getRow()) {
            return from.getCol() - to.getCol() > 0 ? W : E;
        } else if (from.getCol() == to.getCol()) {
            return from.getRow() - to.getRow() > 0 ? N : S;
        } else
            return null; // TODO: are we interested in cases where from-to is on a diagonal path?
    }
}
