package board;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.*;

import htn.Node;
import utils.FibonacciHeap;
import htn.Strategy;

public class LevelService {

    private Level level;
    private FibonacciHeap<Goal> subGoals;

    private BufferedReader in;

    public LevelService(InputStream inputStream) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.subGoals = new FibonacciHeap<Goal>();

        this.level = readLevel();
    }

    /*
    public LinkedList<Node> search(Strategy strategy, Node initialState) {
        strategy.addToFrontier(initialState);
        while (true) {

            if (strategy.frontierIsEmpty()) {
                return null;
            }

            Node leafNode = strategy.getAndRemoveLeaf();

            if (leafNode.isGoalState()) {
                return leafNode.extractPlan();
            }

            strategy.addToExplored(leafNode);
            for (Node n : leafNode.getExpandedNodes()) {
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
        }
    }
    */

    @SuppressWarnings("Duplicates")
    private Level readLevel() throws IOException {
        Map<Character, Color> objectColors;
        String line, color;
        int width = 0, height = 0;
        List<Goal> goals = new ArrayList<>();
        List<Wall> walls = new ArrayList<>();
        List<Agent> agents = new ArrayList<>();
        List<Box> boxes = new ArrayList<>();
        Set<EmptyCell> emptyCells = new HashSet<>();

        objectColors = new HashMap<>();

        // Read lines specifying colors
        while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
            line = line.replaceAll("\\s", "");
            color = line.split(":")[0];

            for (String objectId : line.split(":")[1].split(","))
                objectColors.put(objectId.charAt(0), Color.getColor(color));
        }

        // Read lines specifying level layout
        while (!line.equals("")) {
            int cell;

            for (cell = 0; cell < line.length(); cell++) {
                char id = line.charAt(cell);
                SokobanObject sokobanObject;
                boolean isObjectColorDefined = objectColors.size() > 0 && objectColors.containsKey(id);
                Color objectColor = isObjectColorDefined ? objectColors.get(id) : Color.BLUE;

                // Object in current cell is an Agent
                if ('0' <= id && id <= '9') {
                    sokobanObject = new Agent(height, cell, id, objectColor, SokobanObjectType.AGENT);
                    agents.add((Agent) sokobanObject);
                }

                // Object in current cell is a Goal
                else if ('a' <= id && id <= 'z') {
                    sokobanObject = new Goal(height, cell, id, SokobanObjectType.GOAL);
                    goals.add((Goal) sokobanObject);
                }

                // Object in current cell is a Box
                else if ('A' <= id && id <= 'Z') {
                    sokobanObject = new Box(height, cell, id, objectColor, SokobanObjectType.BOX);
                    boxes.add((Box) sokobanObject);
                }

                // Object in current cell is a Wall
                else if (id == '+') {
                    sokobanObject = new Wall(height, cell, SokobanObjectType.WALL);
                    walls.add((Wall) sokobanObject);
                }

                // Object in current cell is an EmptyCell
                else {
                    emptyCells.add(new EmptyCell(height, cell, SokobanObjectType.EMPTY));
                }
            }
            if (cell > width)
                width = cell;
            height++;

            line = in.readLine();
        }

        return new Level(width, height, goals, boxes, agents, walls, emptyCells);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        List<Agent> agents = this.level.getAgents();
        List<Goal> goals = Level.getGoals();
        List<Box> boxes = this.level.getBoxes();
        List<Wall> walls = Level.getWalls();
        int height = this.level.getHeight();
        int width = this.level.getWidth();
        char[][] board = new char[height][width];

        for (char[] row : board)
            Arrays.fill(row, ' ');

        for (Wall wall : walls)
            board[wall.getCoordinate().getRow()][wall.getCoordinate().getCol()] = '+';

        for (Box box : boxes)
            board[box.getCoordinate().getRow()][box.getCoordinate().getCol()] = box.getBoxType();

        for (Agent agent : agents)
            board[agent.getCoordinate().getRow()][agent.getCoordinate().getCol()] = agent.getAgentType();

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
