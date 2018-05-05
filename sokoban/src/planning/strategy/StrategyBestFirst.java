package planning.strategy;

import architecture.bdi.Desire;
import exceptions.NoValidRefinementsException;
import planning.HTNWorldState;
import planning.actions.Refinement;
import planning.actions.RefinementComparator;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class StrategyBestFirst extends Strategy {

    private Comparator<Refinement> refinementComparator;

    public StrategyBestFirst(Comparator<Refinement> refinementComparator, Desire desire) {
        super(desire);
        this.refinementComparator = refinementComparator;
    }

    @Override
    public Refinement chooseRefinement(List<Refinement> possibleRefinements) throws NoValidRefinementsException {
        // Sort the refinements by priority
        Queue<Refinement> refinements = new PriorityQueue<>(this.refinementComparator);
        refinements.addAll(possibleRefinements);

        // Avoid blacklisted refinements (if no valid refinements are found returns null!)
        Refinement chosenRefinement;
        do {
            chosenRefinement = refinements.poll();
        }
        while (this.refinementsBlacklist.contains(chosenRefinement));

        if (chosenRefinement == null)
            throw new NoValidRefinementsException();

        return chosenRefinement;
    }

    @Override
    public void updateStatus(HTNWorldState worldState) {
        this.refinementComparator = new RefinementComparator(worldState);
    }
}
