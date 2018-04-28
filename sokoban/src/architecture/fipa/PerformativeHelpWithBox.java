package architecture.fipa;

import architecture.AgentThread;
import board.Agent;
import board.Box;
import board.Coordinate;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.io.Console;
import java.util.List;

public class PerformativeHelpWithBox extends Performative {

    private Box box;

    public PerformativeHelpWithBox(Box box, AgentThread asker) {
        this.box = box;
        super.setPerformativeType(PerformativeType.HelpWithBox);
        super.setAsker(asker);
    }

    @Override
    public void execute(AgentThread helper) {
        ConsoleLogger.logInfo(LOGGER, "Agent " + helper.getAgent() +
                " is helping " + getAsker().getAgent() +
                " since he is stuck by " + box);
    }

    @Override
    public FibonacciHeap<AgentThread> findBests(List<AgentThread> agentThreadHelpers, AgentThread agentThread) {
        FibonacciHeap<AgentThread> bests = new FibonacciHeap<>();

        for (AgentThread agentThreadHelper : agentThreadHelpers) {
            //if(!agentThreadHelper.getAgent().getColor().equals(box.getColor())) continue; //TODO uncoment when the box is determined
            if(agentThreadHelper.equals(agentThread)) continue;
            bests.enqueue(agentThreadHelper,
                    Coordinate.manhattanDistance(agentThread.getAgent().getCoordinate(),
                            agentThreadHelper.getAgent().getCoordinate()));
        }

        return bests;
    }
}
