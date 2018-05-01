package planning.relaxations;

import architecture.ClientManager;
import architecture.bdi.Desire;
import board.Color;
import logging.ConsoleLogger;

import java.util.logging.Logger;

public class RelaxationFactory {

    private static final Logger LOGGER = ConsoleLogger.getLogger(RelaxationFactory.class.getSimpleName());

    public static Relaxation getBestPlanningRelaxation(Color agentColor, Desire desire, int planFailureCounter) {
        if (ClientManager.getInstance().getNumberOfAgents() == 1) {
            // Single-Agent
            if (planFailureCounter == 0) {
                // First planning attempt with regular recommended relaxation
                logInfo(NoForeignBoxesRelaxation.class.getSimpleName());
                return new NoForeignBoxesRelaxation(agentColor);
            }
            // If planning with the above relaxation fails, we relax even more to detect the blocking object
            logInfo(OnlyWallsRelaxation.class.getSimpleName());
            return new OnlyWallsRelaxation();
        }

        // Multi-Agent
        logInfo(NoForeignBoxesRelaxation.class.getSimpleName());
        return new NoForeignBoxesRelaxation(agentColor);
    }

    private static void logInfo(String relaxationName) {
        ConsoleLogger.logInfo(LOGGER, "Relaxation type set to " + relaxationName);
    }
}
