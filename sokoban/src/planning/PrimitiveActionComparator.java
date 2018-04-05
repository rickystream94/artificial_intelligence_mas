package planning;

import planning.PrimitiveAction;

import java.util.Comparator;

public class PrimitiveActionComparator implements Comparator<PrimitiveAction> {

    @Override
    public int compare(PrimitiveAction action1, PrimitiveAction action2) {
        Character agent1Id = action1.getAgentId();
        Character agent2Id = action2.getAgentId();
        return agent1Id.compareTo(agent2Id);
    }
}
