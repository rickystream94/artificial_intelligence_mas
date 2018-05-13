package architecture.fipa;

import architecture.agent.AgentThread;
import board.Agent;
import logging.ConsoleLogger;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClearCellRequest extends HelpRequest {

    protected static final Logger LOGGER = ConsoleLogger.getLogger(ClearCellRequest.class.getSimpleName());

    public ClearCellRequest(AgentThread caller, Agent blockingAgent) {
        super(caller, blockingAgent);
    }

    @Override
    protected void chooseHelper(AgentThread helper) {
        ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: asking Agent %c to free the cell %s.", getCaller().getAgent().getAgentId(), helper.getAgent().getAgentId(), getBlockingObject().getCoordinate()));
        helper.getHelpRequestResolver().addHelpRequest(this);
    }

    @Override
    protected AgentThread findBestHelper(List<AgentThread> helpers) {
        return helpers.stream().filter(helper -> helper.getAgent().equals(getBlockingObject())).collect(Collectors.toList()).get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(super.equals(o)))
            return false;
        if (!(o instanceof ClearCellRequest))
            return false;
        ClearCellRequest request = (ClearCellRequest) o;
        Agent blockingAgent = (Agent) request.getBlockingObject();
        return blockingAgent.equals(this.getBlockingObject());
    }
}
