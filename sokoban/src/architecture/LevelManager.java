package architecture;

import board.Agent;
import board.Box;
import board.Level;
import logging.ConsoleLogger;
import planning.actions.Effect;
import planning.actions.PrimitiveTask;

import java.util.Objects;
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
        return Level.getGoals().stream()
                .allMatch(goal -> this.level.getBoxes().stream() // Each goal must have a box of same type that solves it
                        .filter(box -> Character.toLowerCase(box.getBoxType()) == goal.getGoalType())
                        .anyMatch(box -> box.getCoordinate().equals(goal.getCoordinate())));
    }

    public void applyAction(PrimitiveTask task, Agent agent) {
        Box box = agent.getCurrentTargetBox();
        Effect effect = task.getEffect(agent.getCoordinate(), box != null ? box.getCoordinate() : null);
        if (effect == null)
            return;
        handleEmptyCells(task, effect, agent, box);

        // Change positions of objects
        level.getAgentsMap().remove(agent.getCoordinate());
        level.getAgentsMap().put(agent.getCoordinate(), agent);
        agent.setCoordinate(effect.getNewAgentPosition());
        if (effect.getNewBoxPosition() != null && box != null) {
            level.getBoxesMap().remove(box.getCoordinate());
            level.getBoxesMap().put(box.getCoordinate(), box);
            box.setCoordinate(effect.getNewBoxPosition());
        }
    }

    private void handleEmptyCells(PrimitiveTask task, Effect effect, Agent agent, Box box) {
        switch (task.getType()) {
            case Move:
                level.addEmptyCell(agent.getCoordinate());
                level.removeEmptyCell(effect.getNewAgentPosition());
                break;
            case Push:
                level.addEmptyCell(agent.getCoordinate());
                level.removeEmptyCell(effect.getNewBoxPosition());
                break;
            case Pull:
                level.addEmptyCell(Objects.requireNonNull(box).getCoordinate());
                level.removeEmptyCell(effect.getNewAgentPosition());
                break;
            default:
                break;
        }
    }

    public Level getLevel() {
        return this.level;
    }
}
