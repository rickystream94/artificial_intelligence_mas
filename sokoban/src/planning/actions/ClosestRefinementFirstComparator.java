package planning.actions;

import planning.HTNWorldState;

public class ClosestRefinementFirstComparator extends RefinementComparator {

    public ClosestRefinementFirstComparator(HTNWorldState worldState) {
        super(worldState);
    }

    @Override
    public int compare(Refinement r1, Refinement r2) {
        int cost1 = r1.computeCost(this.worldState);
        int cost2 = r2.computeCost(this.worldState);
        return cost1 - cost2;
    }
}
