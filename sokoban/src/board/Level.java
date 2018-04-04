package board;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class Level {

    private static int width;
    private static int height;
    private static List<Goal> goals;
    private static List<Wall> walls;
    private List<Agent> agents;
    private List<Box> boxes;
    private Set<EmptyCell> emptyCells;

    public Level(int width, int height, List<Goal> goals, List<Box> boxes, List<Agent> agents, List<Wall> walls, Set<EmptyCell> emptyCells) {
        Level.width = width;
        Level.height = height;
        Level.goals = new ArrayList<>(goals);
        Level.walls = new ArrayList<>(walls);
        this.agents = new ArrayList<>(agents);
        this.boxes = new ArrayList<>(boxes);
        this.emptyCells = new HashSet<>(emptyCells);
    }

    public static List<Goal> getGoals() {
        return Level.goals;
    }

    public static List<Wall> getWalls() {
        return Level.walls;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public boolean isCellEmpty(Coordinate coordinate) {
        EmptyCell emptyCell = new EmptyCell(coordinate.getRow(), coordinate.getCol(), SokobanObjectType.EMPTY);
        return emptyCells.contains(emptyCell);
    }

    public void removeEmptyCell(Coordinate coordinate) {
        emptyCells.remove(new EmptyCell(coordinate.getRow(), coordinate.getCol(), SokobanObjectType.EMPTY));
    }

    public void addEmptyCell(Coordinate coordinate) {
        emptyCells.add(new EmptyCell(coordinate.getRow(), coordinate.getCol(), SokobanObjectType.EMPTY));
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
