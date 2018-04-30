package planning.relaxations;

import architecture.ClientManager;
import architecture.LevelManager;
import board.Color;

public class RelaxationFactory {

    public static Relaxation getBestRelaxation(Color agentColor) {
        LevelManager levelManager = ClientManager.getInstance().getLevelManager();
        if (ClientManager.getInstance().getNumberOfAgents() == 1)
            // Single-Agent
            return new AllObjectsRelaxation(levelManager);

        // Multi-Agent
        return new NoForeignBoxesRelaxation(levelManager, agentColor);

        // TODO: there might be more cases/relaxations depending on the situation... to be defined
    }
}
