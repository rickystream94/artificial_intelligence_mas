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

    public synchronized void dispatchPerformative(HelpRequest helpRequest) {
        FibonacciHeap<AgentThread> helpersWithPriorities = helpRequest.findBestHelpers(listeners, helpRequest.getCaller());
        if (!helpersWithPriorities.isEmpty()) {
            AgentThread helper = helpersWithPriorities.dequeueMin().getValue();
            helpRequest.chooseHelper(helper);
        }
    }
}
