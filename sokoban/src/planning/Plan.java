package planning;

import planning.actions.AbstractTask;

import java.util.List;

public interface Plan<T extends AbstractTask> {
    List<? extends T> getActions();
}
