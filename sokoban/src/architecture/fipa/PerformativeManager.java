package architecture.fipa;

import architecture.agent.AgentThread;
import utils.FibonacciHeap;

import java.util.ArrayList;
import java.util.List;

public class PerformativeManager {
    private List<AgentThread> listeners;

    private static PerformativeManager instance;

    private PerformativeManager() { listeners = new ArrayList<>(); }

    public static PerformativeManager getDefault() {
        if (instance == null)
            instance = new PerformativeManager();
        return instance;
    }

    public void register(AgentThread agentThread) {
        listeners.add(agentThread);
    }

    public void execute(Performative performative) {
        FibonacciHeap<AgentThread> helpers = performative.findBests(listeners,performative.getAsker());
        while(!helpers.isEmpty()) {
            AgentThread helper = helpers.dequeueMin().getValue();
            performative.execute(helper);
        }
    }
}
