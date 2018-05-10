package architecture.fipa;

import architecture.agent.AgentThread;
import architecture.agent.HelpWithBoxRequest;
import board.Box;
import board.Coordinate;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.List;
import java.util.logging.Logger;

public class PerformativeHelpWithBox extends Performative {

    protected static final Logger LOGGER = ConsoleLogger.getLogger(Performative.class.getSimpleName());

    private Box blockingBox;

    public PerformativeHelpWithBox(Box blockingBox, AgentThread caller) {
        super(caller);
        this.blockingBox = blockingBox;
    }

    @Override
    public void execute(AgentThread helper) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: chosen Agent %c as helper to clear box %s.", getCaller().getAgent().getAgentId(), helper.getAgent().getAgentId(), blockingBox));
        helper.getHelpRequestResolver().addHelpRequest(new HelpWithBoxRequest(blockingBox, getCaller()));
    }

    @Override
    public FibonacciHeap<AgentThread> findBestHelpers(List<AgentThread> helpers, AgentThread caller) {
        FibonacciHeap<AgentThread> bests = new FibonacciHeap<>();

        // Filter only agents that can move the box
        helpers.stream().filter(h -> h.getAgent().getColor() == blockingBox.getColor() && !h.equals(caller))
                .forEach(h -> {
                    int priority = Coordinate.manhattanDistance(blockingBox.getCoordinate(), h.getAgent().getCoordinate()) + helperPriorityByStatus(h);
                    bests.enqueue(h, priority);
                });
        return bests;
    }
}
