package planning.relaxations;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Color;

public class RelaxationFactory {

    public static Relaxation getBestPlanningRelaxation(Color agentColor, Desire desire) {
        LevelManager levelManager = ClientManager.getInstance().getLevelManager();
        if (ClientManager.getInstance().getNumberOfAgents() == 1) {
            // Single-Agent
            if (desire instanceof GoalDesire)
                return new OnlyWallsRelaxation(levelManager);
            else if (desire instanceof ClearPathDesire)
                return new NoForeignBoxesRelaxation(levelManager, agentColor);
        }

        // Multi-Agent
        return new NoForeignBoxesRelaxation(levelManager, agentColor);

        // TODO: there might be more cases/relaxations depending on the situation... to be defined
    }

    public static Relaxation getMonitoringRelaxation(Color agentColor) {
        LevelManager levelManager = ClientManager.getInstance().getLevelManager();
        return new NoForeignBoxesRelaxation(levelManager, agentColor);
    }
}
