package architecture;

import board.Box;
import board.Coordinate;
import board.Goal;
import board.Level;
import exceptions.NotImplementedException;
import logging.ConsoleLogger;

import java.util.logging.Logger;

/**
 * All changes to the level should be handled by this class, as a direct consequence of the joint actions result
 */
public class LevelManager {

    private static final Logger LOGGER = ConsoleLogger.getLogger(LevelManager.class.getSimpleName());
    private Level level;

    public LevelManager(Level level) {
        this.level = level;
    }

    /***
     * Checks that all boxes are placed in the corresponding goals
     * @return true if level is solved, false otherwise
     * @throws NotImplementedException
     */
    public boolean isLevelSolved() throws NotImplementedException {
        throw new NotImplementedException();
        // TODO: to be implemented
    }

    public boolean isGoalState() {
        for (Box box : this.level.getBoxes()) {
            for (Goal goal : Level.getGoals()) {
                Coordinate boxPosition = box.getCoordinate();
                Coordinate goalPosition = goal.getCoordinate();
                if (goalPosition.getRow() == boxPosition.getRow() && goalPosition.getCol() == boxPosition.getCol()
                        && Character.toLowerCase(box.getBoxType()) == goal.getGoalType()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Level getLevel() {
        return this.level;
    }
}
