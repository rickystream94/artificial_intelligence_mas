package board;

import java.util.*;

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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        char[][] board = new char[height][width];

        for (char[] row : board)
            Arrays.fill(row, ' ');

        for (Wall wall : walls)
            board[wall.getCoordinate().getRow()][wall.getCoordinate().getCol()] = '+';

        for (Box box : boxes)
            board[box.getCoordinate().getRow()][box.getCoordinate().getCol()] = box.getBoxType();

        for (Agent agent : agents)
            board[agent.getCoordinate().getRow()][agent.getCoordinate().getCol()] = agent.getAgentId();

        for (Goal goal : goals)
            board[goal.getCoordinate().getRow()][goal.getCoordinate().getCol()] = goal.getGoalType();

        s.append(String.format("Dimensions: (%d,%d)\n", height, width));

        for (char[] row : board) {
            for (char cell : row) {
                s.append(cell);
            }
            s.append('\n');
        }

        return s.toString();
    }
}
