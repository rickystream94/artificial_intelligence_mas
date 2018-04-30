package board;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Level {

    /*
     * All static components won't change throughout the program
     */
    private static int width;
    private static int height;
    private static Map<Coordinate, Goal> goalsMap;
    private static Map<Coordinate, Wall> wallsMap;

    private Set<EmptyCell> emptyCells;
    private Map<Coordinate, Agent> agentsMap;
    private Map<Coordinate, Box> boxesMap;

    /**
     * All data structures are thread-safe to guarantee concurrent access from the agent threads
     *
     * @param width      board width
     * @param height     board height
     * @param goals      list of goals
     * @param boxes      list of boxes
     * @param agents     list of agents
     * @param walls      list of walls
     * @param emptyCells list of empty cells
     */
    public Level(int width, int height, List<Goal> goals, List<Box> boxes, List<Agent> agents, List<Wall> walls, Set<EmptyCell> emptyCells) {
        Level.width = width;
        Level.height = height;
        Level.goalsMap = new ConcurrentHashMap<>();
        Level.wallsMap = new ConcurrentHashMap<>();
        this.agentsMap = new ConcurrentHashMap<>();
        this.boxesMap = new ConcurrentHashMap<>();
        this.emptyCells = ConcurrentHashMap.newKeySet();
        this.emptyCells.addAll(emptyCells);

        // Build coordinate hash maps
        agents.forEach(agent -> this.agentsMap.put(agent.getCoordinate(), agent));
        boxes.forEach(box -> this.boxesMap.put(box.getCoordinate(), box));
        goals.forEach(goal -> Level.goalsMap.put(goal.getCoordinate(), goal));
        walls.forEach(wall -> Level.wallsMap.put(wall.getCoordinate(), wall));
    }

    public static List<Goal> getGoals() {
        return new ArrayList<>(Level.goalsMap.values());
    }

    public static boolean isGoalCell(Coordinate coordinate) {
        return Level.goalsMap.containsKey(coordinate);
    }

    public static boolean isNotWall(Coordinate coordinate) {
        return !Level.wallsMap.containsKey(coordinate);
    }

    public List<Agent> getAgents() {
        return new ArrayList<>(this.agentsMap.values());
    }

    public Map<Coordinate, Agent> getAgentsMap() {
        return this.agentsMap;
    }

    public List<Box> getBoxes() {
        return new ArrayList<>(this.boxesMap.values());
    }

    public List<Box> getOtherBoxesOfSameColor(Box box) {
        return getBoxes().stream()
                .filter(b -> b.getColor() == box.getColor() && !b.equals(box))
                .collect(Collectors.toList());
    }

    public Map<Coordinate, Box> getBoxesMap() {
        return this.boxesMap;
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

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public Set<Coordinate> getAllPlayableCells() {
        Set<Coordinate> allCells = new HashSet<>();
        allCells.addAll(emptyCells.stream().map(SokobanObject::getCoordinate).collect(Collectors.toList()));
        allCells.addAll(getBoxes().stream().map(SokobanObject::getCoordinate).collect(Collectors.toList()));
        allCells.addAll(getAgents().stream().map(SokobanObject::getCoordinate).collect(Collectors.toList()));
        return allCells;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        char[][] board = new char[height][width];

        for (char[] row : board)
            Arrays.fill(row, ' ');

        for (Wall wall : Level.wallsMap.values())
            board[wall.getCoordinate().getRow()][wall.getCoordinate().getCol()] = '+';

        for (Box box : this.boxesMap.values())
            board[box.getCoordinate().getRow()][box.getCoordinate().getCol()] = box.getBoxType();

        for (Agent agent : this.agentsMap.values())
            board[agent.getCoordinate().getRow()][agent.getCoordinate().getCol()] = agent.getAgentId();

        for (Goal goal : Level.goalsMap.values())
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
