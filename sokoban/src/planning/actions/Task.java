package planning.actions;

public interface Task<T extends Enum<T>> {
    T getType();

    @Override
    boolean equals(Object other);
}
