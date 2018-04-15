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
        // TODO heuristic calculation comparison
        return 0;
    }
}
