package planning.strategy;

import exceptions.NoValidRefinementsException;
import planning.actions.Refinement;
import planning.actions.Task;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class StrategyBestFirst extends Strategy {

    private Queue<Refinement> refinements;

    public StrategyBestFirst(Comparator<Refinement> refinementComparator, Task rootTask) {
        super(rootTask);
        this.refinements = new PriorityQueue<>(refinementComparator);
    }

    @Override
    public Refinement chooseRefinement(List<Refinement> possibleRefinements) throws NoValidRefinementsException {
        // Sort the refinements by priority
        this.refinements.addAll(possibleRefinements);

        // Avoid blacklisted refinements (if no valid refinements are found returns null!)
        Refinement chosenRefinement;
        do {
            chosenRefinement = this.refinements.poll();
        }
        while (!this.refinementsBlacklist.contains(chosenRefinement));

        // Clear queue
        this.refinements.clear();

        if (chosenRefinement == null)
            throw new NoValidRefinementsException();

        return chosenRefinement;
    }
}
