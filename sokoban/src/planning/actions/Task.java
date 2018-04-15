package planning.actions;

import planning.HTNWorldState;

public interface Task<T extends Enum<T>> {
    T getType();

    int calculateApproximation(HTNWorldState worldState);
}
