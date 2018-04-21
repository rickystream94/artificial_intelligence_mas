package architecture;

import board.*;
import exceptions.NotImplementedException;
import logging.ConsoleLogger;
import planning.actions.Effect;
import planning.actions.PrimitiveTask;

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
     */
    public boolean isLevelSolved() {
        for (Box box : this.level.getBoxes()) {
            for (Goal goal : Level.getGoals()) {
                Coordinate boxPosition = box.getCoordinate();
                Coordinate goalPosition = goal.getCoordinate();
                if (goalPosition.getRow() == boxPosition.getRow() && goalPosition.getCol() == boxPosition.getCol()
                        && Character.toLowerCase(box.getBoxType()) != goal.getGoalType()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void moveAgent(Effect effect, Agent agent){
        level.addEmptyCell(agent.getCoordinate());
        level.removeEmptyCell(effect.getNewAgentPosition());
        level.getAgentsMap().remove(agent.getCoordinate());
        agent.setCoordinate(effect.getNewAgentPosition());
        level.getAgentsMap().put(agent.getCoordinate(),agent);
    }

    private void moveBox(Effect effect, Box box){
        level.addEmptyCell(box.getCoordinate());
        level.removeEmptyCell(effect.getNewBoxPosition());
        level.getBoxesMap().remove(box.getCoordinate());
        box.setCoordinate(effect.getNewBoxPosition());
        level.getBoxesMap().put(box.getCoordinate(),box);
    }

    public void applyAction(SendActionEvent action){
        Agent agent = action.getAgent();
        PrimitiveTask task = action.getAction();
        Box box = null;

        Effect effect = task.getMoveEffect(agent.getCoordinate());
        moveAgent(effect,agent);

        switch (task.getType()){
            case Push:
                box = level.getBoxesMap().get(effect.getNewAgentPosition());
                break;
            case Pull:
                box = level.getBoxesMap().get(agent.getCoordinate());
                break;
        }

        if(box != null) {
            effect = task.getEffect(agent.getCoordinate(),box.getCoordinate());
            moveBox(effect,box);
        }

    }

    public Level getLevel() {
        return this.level;
    }
}
