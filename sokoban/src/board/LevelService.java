package board;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

import htn.Node;
import utils.FibonacciHeap;
import htn.Strategy;

/**
 * This class is in charge of handling the communication between the client and the server, hence reading server messages and sending joint actions
 */
public class LevelService {

    private Level level;
    private FibonacciHeap<Goal> subGoals;

    private BufferedReader in;

    public LevelService(InputStream inputStream) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.level = new Level();
        this.subGoals = new FibonacciHeap<Goal>();

        readLevel();
    }

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

    @SuppressWarnings("Duplicates")
    private void readLevel() throws IOException {
        Map<Character, String> objectColors;
        String line, color;
        int width = 0, height = 0;

        objectColors = new HashMap<>();

        // Read lines specifying colors
        while ((line = in.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
            line = line.replaceAll("\\s", "");
            color = line.split(":")[0];

            for (String objectId : line.split(":")[1].split(","))
                objectColors.put(objectId.charAt(0), color);
        }

        // Read lines specifying level layout
        while (!line.equals("")) {
            int cell;

            for (cell = 0; cell < line.length(); cell++) {
                char id = line.charAt(cell);
                SokobanObject sokobanObject;
                boolean isObjectColorDefined = objectColors.size() > 0 && objectColors.containsKey(id);
                String objectColor = isObjectColorDefined ? objectColors.get(id).toUpperCase() : Colors.BLUE.toString();

                // Object in current cell is an Agent
                if ('0' <= id && id <= '9') {
                    sokobanObject = new Agent(height, cell, id, objectColor, SokobanObjectType.AGENT);
                    this.level.addAgent((Agent) sokobanObject);
                }

                // Object in current cell is a Goal
                else if ('a' <= id && id <= 'z') {
                    sokobanObject = new Goal(height, cell, id, SokobanObjectType.GOAL);
                    this.level.addGoal((Goal) sokobanObject);
                }

                // Object in current cell is a Box
                else if ('A' <= id && id <= 'Z') {
                    sokobanObject = new Box(height, cell, id, objectColor, SokobanObjectType.BOX);
                    this.level.addBox((Box) sokobanObject);
                }

                // Object in current cell is a Wall
                else if (id == '+') {
                    sokobanObject = new Wall(height, cell, SokobanObjectType.WALL);
                    this.level.addWall((Wall) sokobanObject);
                }

                // Object in current cell is an EmptyCell
                else {
                    this.level.addEmptyCell(new Coordinate(height, cell));
                }
            }
            if (cell > width)
                width = cell;
            height++;

            line = in.readLine();
        }

        Level.setHeight(height);
        Level.setWidth(width);
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
