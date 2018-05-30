package architecture.bdi;

import board.Goal;

import java.util.Comparator;

public class GoalComparator implements Comparator<Goal> {

    @Override
    public int compare(Goal g1, Goal g2) {
        return g2.getPriority() - g1.getPriority();
    }
}
