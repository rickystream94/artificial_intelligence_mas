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
                return new Coordinate(from.getRow(), from.getCol() + 1);
            case S:
                return new Coordinate(from.getRow(), from.getCol() - 1);
            case E:
                return new Coordinate(from.getRow() + 1, from.getCol());
            case W:
                return new Coordinate(from.getRow() - 1, from.getCol());
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
}
