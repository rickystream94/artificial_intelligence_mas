package planning.relaxations;

import architecture.ClientManager;
import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import board.Color;

public class RelaxationFactory {

    public static Relaxation getBestPlanningRelaxation(Color agentColor, Desire desire, int planFailureCounter) {
        if (ClientManager.getInstance().getNumberOfAgents() == 1) {
            // Single-Agent
            if (desire instanceof GoalDesire) {
                if (planFailureCounter == 0)
                    // First planning attempt with regular recommended relaxation
                    return new NoForeignBoxesRelaxation(agentColor);
                // If planning with the above relaxation fails, we relax even more to detect the blocking object
                return new OnlyWallsRelaxation();
            } else if (desire instanceof ClearPathDesire)
                return new NoForeignBoxesRelaxation(agentColor);
        }

        // Multi-Agent
        return new NoForeignBoxesRelaxation(agentColor);

        // TODO: there might be more cases/relaxations depending on the situation... to be defined
    }
}
