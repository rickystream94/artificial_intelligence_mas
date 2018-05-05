package board;

import logging.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class BoardReader {

    private static final Logger LOGGER = ConsoleLogger.getLogger(BoardReader.class.getSimpleName());

    public static Level readLevel(BufferedReader reader) throws IOException {
        ConsoleLogger.logInfo(LOGGER, "Parsing level from Server...");

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
        while ((line = reader.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
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
                    sokobanObject = new Agent(Level.getAvailableObjectId(), height, cell, id, objectColor);
                    agents.add((Agent) sokobanObject);
                }

                // Object in current cell is a Goal
                else if ('a' <= id && id <= 'z') {
                    sokobanObject = new Goal(Level.getAvailableObjectId(), height, cell, id);
                    goals.add((Goal) sokobanObject);
                    emptyCells.add(new EmptyCell(Level.getAvailableObjectId(), height, cell)); // Goals are empty cells!
                }

                // Object in current cell is a Box
                else if ('A' <= id && id <= 'Z') {
                    sokobanObject = new Box(Level.getAvailableObjectId(), height, cell, id, objectColor);
                    boxes.add((Box) sokobanObject);
                }

                // Object in current cell is a Wall
                else if (id == '+') {
                    sokobanObject = new Wall(Level.getAvailableObjectId(), height, cell);
                    walls.add((Wall) sokobanObject);
                }

                // Object in current cell is an EmptyCell
                else {
                    emptyCells.add(new EmptyCell(Level.getAvailableObjectId(), height, cell));
                }
            }
            if (cell > width)
                width = cell;
            height++;

            line = reader.readLine();
        }

        ConsoleLogger.logInfo(LOGGER, "Done reading level from Server");

        return new Level(width, height, goals, boxes, agents, walls, emptyCells);
    }
}
