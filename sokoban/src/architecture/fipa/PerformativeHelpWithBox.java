package architecture.fipa;

import architecture.agent.AgentThread;
import architecture.bdi.Desire;
import board.Box;
import board.Coordinate;
import exceptions.NoProgressException;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.List;
import java.util.logging.Logger;

public class PerformativeHelpWithBox extends Performative {

    protected static final Logger LOGGER = ConsoleLogger.getLogger(Performative.class.getSimpleName());

    private Box box;

    public PerformativeHelpWithBox(Box box, AgentThread caller) {
        super(caller);
        this.box = box;
    }

    @Override
    public void execute(AgentThread helper) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: helping Agent %c by clearing box %s.", getCaller().getAgent().getAgentId(), helper.getAgent().getAgentId(), box));
        try {
            Desire clearPathDesire = helper.getLockDetector().handleBlockingBox(box);
            helper.addHelpRequest(clearPathDesire);
        } catch (NoProgressException e) {
            e.printStackTrace(); // TODO: should not occur... hopefully
        }
    }

    @Override
    public FibonacciHeap<AgentThread> findBests(List<AgentThread> helpers, AgentThread caller) {
        FibonacciHeap<AgentThread> bests = new FibonacciHeap<>();

        // Filter only agents that can move the box
        helpers.stream().filter(h -> h.getAgent().getColor() == box.getColor() && !h.equals(caller))
                .forEach(h -> {
                    int priority = Coordinate.manhattanDistance(box.getCoordinate(), h.getAgent().getCoordinate()) + helperPriorityByStatus(h);
                    bests.enqueue(h, priority);
                });
        return bests;
    }
}
