package planning.actions;

import planning.HTNWorldState;

import java.util.Comparator;

public class RefinementComparator implements Comparator<Refinement> {

    private final HTNWorldState worldState;

    public RefinementComparator(HTNWorldState worldState) {
        this.worldState = worldState;
    }

    @Override
    public int compare(Refinement r1, Refinement r2) {
        int cost1 = r1.computeCost(this.worldState);
        int cost2 = r2.computeCost(this.worldState);
        return cost1 - cost2;
    }
}
