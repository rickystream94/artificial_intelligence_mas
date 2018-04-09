package board;

public enum Color {
    BLUE, RED, GREEN, CYAN, MAGENTA, ORANGE, PINK, YELLOW;

    public static Color getColor(String color) {
        switch (color) {
            case "blue":
                return Color.BLUE;
            case "red":
                return Color.RED;
            case "green":
                return Color.GREEN;
            case "cyan":
                return Color.CYAN;
            case "magenta":
                return Color.MAGENTA;
            case "orange":
                return Color.ORANGE;
            case "pink":
                return Color.PINK;
            case "yellow":
                return Color.YELLOW;
            default:
                return null;
        }
    }
}
