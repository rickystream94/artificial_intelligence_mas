package planning.actions;

import planning.HTNWorldState;

import java.util.Comparator;

public abstract class RefinementComparator implements Comparator<Refinement> {

    protected final HTNWorldState worldState;

    public RefinementComparator(HTNWorldState worldState) {
        this.worldState = worldState;
    }

    @Override
    public abstract int compare(Refinement r1, Refinement r2);
}
