package planning.actions;

import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import planning.HTNWorldState;

public class RefinementsComparatorFactory {

    public static RefinementComparator getComparator(Desire desire, HTNWorldState worldState) {
        if (desire instanceof GoalDesire)
            return new ClosestRefinementFirstComparator(worldState);
        else if (desire instanceof ClearPathDesire)
            return new ClosestRefinementFirstComparator(worldState); // TODO revert because useless
        return null;
    }
}
