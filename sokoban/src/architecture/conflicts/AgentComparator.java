package architecture.conflicts;

import architecture.agent.AgentThread;

import java.util.Comparator;

public class AgentComparator implements Comparator<AgentThread> {

    @Override
    public int compare(AgentThread a1, AgentThread a2) {
        Character agentId1 = a1.getAgent().getAgentId();
        Character agentId2 = a2.getAgent().getAgentId();
        return agentId1.compareTo(agentId2);
    }
}
