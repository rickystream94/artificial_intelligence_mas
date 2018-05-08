package architecture.protocol;

import java.util.Comparator;

public class SendActionEventComparator implements Comparator<SendActionEvent> {

    @Override
    public int compare(SendActionEvent sendActionEvent1, SendActionEvent sendActionEvent2) {
        Character agent1Id = sendActionEvent1.getAgent().getAgentId();
        Character agent2Id = sendActionEvent2.getAgent().getAgentId();
        return agent1Id.compareTo(agent2Id);
    }
}
