package architecture;

import java.util.Comparator;

public class AgentActionComparator implements Comparator<AgentAction> {

    @Override
    public int compare(AgentAction action1, AgentAction action2) {
        Character agent1Id = action1.getAgent().getAgentId();
        Character agent2Id = action1.getAgent().getAgentId();
        return agent1Id.compareTo(agent2Id);
    }
}
