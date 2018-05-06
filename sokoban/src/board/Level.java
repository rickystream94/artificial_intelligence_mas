package board;

import architecture.bdi.Desire;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Level {

    /*
     * All static components won't change throughout the program
     */
    private static int width;
    private static int height;
    private static int availableObjectId = 0;
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

        sanityCheck();
    }

    public static int getAvailableObjectId() {
        return availableObjectId++;
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

    public Map<Coordinate, Box> getBoxesMap() {
        return this.boxesMap;
    }

    public boolean isCellEmpty(Coordinate coordinate) {
        return emptyCells.stream().anyMatch(emptyCell -> emptyCell.getCoordinate().equals(coordinate));
    }

    public void removeEmptyCell(Coordinate coordinate) {
        emptyCells.removeIf(emptyCell -> emptyCell.getCoordinate().equals(coordinate));
    }

    public void addEmptyCell(Coordinate coordinate) {
        emptyCells.add(new EmptyCell(Level.getAvailableObjectId(), coordinate.getRow(), coordinate.getCol()));
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public Set<Coordinate> getEmptyCellsPositions() {
        return this.emptyCells.stream().map(SokobanObject::getCoordinate).collect(Collectors.toSet());
    }

    public SokobanObject dynamicObjectAt(Coordinate coordinate) {
        if (agentsMap.containsKey(coordinate))
            return agentsMap.get(coordinate);
        if (boxesMap.containsKey(coordinate))
            return boxesMap.get(coordinate);
        return null;
    }

    public boolean isDesireAchieved(Desire desire) {
        Box box = desire.getBox();
        Coordinate targetPosition = desire.getTarget();
        return box.getCoordinate().equals(targetPosition);
    }

    private void sanityCheck() {

        // NORTH
        for(int col = 0; col<width; col++) {
            for(int foundRow = 0; foundRow < height; foundRow++) {
                if(this.wallsMap.containsKey(new Coordinate(foundRow, col))) {
                    for(int row = foundRow - 1; row >= 0; row--)
                        removeEmptyCell(new Coordinate(row, col));
                    break;
                }
            }
        }

        //EAST
        for(int row = 0; row < height; row++) {
            for(int foundCol = 0; foundCol < width; foundCol++) {
                if(this.wallsMap.containsKey(new Coordinate(row,foundCol))) {
                    for(int col = foundCol - 1; col >= 0; col--)
                        removeEmptyCell(new Coordinate(row, col));
                    break;
                }
            }
        }

        // SOUTH
        for(int col = 0; col < width; col++) {
            for(int foundRow = height-1; foundRow>=0; foundRow--) {
                if(this.wallsMap.containsKey(new Coordinate(foundRow,col))) {
                    for(int row = foundRow; row < height; row++)
                        removeEmptyCell(new Coordinate(row, col));
                    break;
                }
            }
        }

        //WEST
        for(int row = 0; row < height; row++) {
            for(int foundCol = width - 1; foundCol >= 0; foundCol--) {
                if(this.wallsMap.containsKey(new Coordinate(row,foundCol))) {
                    for(int col = foundCol; col < width; col++)
                        removeEmptyCell(new Coordinate(row, col));
                    break;
                }
            }
        }
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
