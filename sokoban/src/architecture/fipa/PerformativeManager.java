package architecture.fipa;

import architecture.agent.AgentThread;
import utils.FibonacciHeap;

import java.util.ArrayList;
import java.util.List;

public class PerformativeManager {

    private static PerformativeManager instance;
    private List<AgentThread> listeners;

    private PerformativeManager() {
        listeners = new ArrayList<>();
    }

    public static PerformativeManager getDefault() {
        if (instance == null)
            instance = new PerformativeManager();
        return instance;
    }

    public void register(AgentThread agentThread) {
        listeners.add(agentThread);
    }

    public void execute(Performative performative) {
        FibonacciHeap<AgentThread> helpersWithPriorities = performative.findBestHelpers(listeners, performative.getCaller());
        if (!helpersWithPriorities.isEmpty()) {
            AgentThread helper = helpersWithPriorities.dequeueMin().getValue();
            performative.execute(helper);
        }
    }
}
