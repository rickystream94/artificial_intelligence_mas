package architecture.fipa;

import architecture.agent.AgentThread;
import board.Box;
import board.Coordinate;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.util.List;
import java.util.logging.Logger;

public class ClearBoxRequest extends HelpRequest {

    protected static final Logger LOGGER = ConsoleLogger.getLogger(ClearBoxRequest.class.getSimpleName());

    public ClearBoxRequest(AgentThread caller, Box blockingBox) {
        super(caller, blockingBox);
    }

    @Override
    public void chooseHelper(AgentThread helper) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: chosen Agent %c as helper to clear box %s.", getCaller().getAgent().getAgentId(), helper.getAgent().getAgentId(), getBlockingObject()));
        helper.getHelpRequestResolver().addHelpRequest(this);
    }

    @Override
    public AgentThread findBestHelper(List<AgentThread> helpers) {
        FibonacciHeap<AgentThread> bests = new FibonacciHeap<>();

        // Filter only agents that can move the box
        helpers.stream().filter(h -> h.getAgent().getColor() == ((Box) getBlockingObject()).getColor() && !h.equals(getCaller()))
                .forEach(h -> {
                    int priority = Coordinate.manhattanDistance(getBlockingObject().getCoordinate(), h.getAgent().getCoordinate()) + helperPriorityByStatus(h);
                    bests.enqueue(h, priority);
                });
        return bests.dequeueMin().getValue();
    }
}
