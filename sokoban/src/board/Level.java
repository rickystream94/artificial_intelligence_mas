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

    public Level() {
        goals = new ArrayList<>();
        walls = new ArrayList<>();
        this.agents = new ArrayList<>();
        this.boxes = new ArrayList<>();
        this.emptyCells = new HashSet<>();
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

    public void addAgent(Agent agent) {
        this.agents.add(agent);
    }

    public void addBox(Box box) {
        this.boxes.add(box);
    }

    public void addGoal(Goal goal) {
        Level.goals.add(goal);
    }

    public void addWall(Wall wall) {
        Level.walls.add(wall);
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

    public static void setWidth(int width) {
        Level.width = width;
    }

    public static void setHeight(int height) {
        Level.height = height;
    }
}
