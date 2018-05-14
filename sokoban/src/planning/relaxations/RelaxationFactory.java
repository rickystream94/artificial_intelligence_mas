package planning.relaxations;

import architecture.ClientManager;
import board.Agent;
import logging.ConsoleLogger;

import java.util.logging.Logger;

public class RelaxationFactory {

    private static final Logger LOGGER = ConsoleLogger.getLogger(RelaxationFactory.class.getSimpleName());

    // TODO: this is not beautiful, it can be improved
    public static Relaxation getBestPlanningRelaxation(Agent agent, int planFailureCounter) {
        if (ClientManager.getInstance().getNumberOfAgents() == 1) {
            // Single-Agent
            switch (planFailureCounter) {
                case 0:
                    logInfo(agent, MyBoxesAndWallsRelaxation.class.getSimpleName());
                    return new MyBoxesAndWallsRelaxation(agent.getColor());
                default:
                    logInfo(agent, OnlyWallsRelaxation.class.getSimpleName());
                    return new OnlyWallsRelaxation();
            }
        }

        // Multi-Agent
        switch (planFailureCounter) {
            case 0:
                logInfo(agent, NoAgentsRelaxation.class.getSimpleName());
                return new NoAgentsRelaxation();
            case 1:
                logInfo(agent, MyBoxesAndWallsRelaxation.class.getSimpleName());
                return new MyBoxesAndWallsRelaxation(agent.getColor());
            case 2:
                logInfo(agent, ForeignBoxesAndWallsRelaxation.class.getSimpleName());
                return new ForeignBoxesAndWallsRelaxation(agent.getColor());
            case 3:
                logInfo(agent, OnlyWallsRelaxation.class.getSimpleName());
                return new OnlyWallsRelaxation();
            default:
                return new OnlyWallsRelaxation();
        }
    }

    private static void logInfo(Agent agent, String relaxationName) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: Relaxation type set to %s", agent.getAgentId(), relaxationName));
    }
}
