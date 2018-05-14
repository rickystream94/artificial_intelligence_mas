package architecture.fipa;

import architecture.agent.AgentThread;

import java.util.ArrayList;
import java.util.List;

public class PerformativeManager {

    private static PerformativeManager instance;
    private List<AgentThread> listeners;

    private PerformativeManager() {
        listeners = new ArrayList<>();
    }

    public synchronized static PerformativeManager getDefault() {
        if (instance == null)
            instance = new PerformativeManager();
        return instance;
    }

    public void register(AgentThread agentThread) {
        listeners.add(agentThread);
    }

    public synchronized void dispatchPerformative(HelpRequest helpRequest) {
        AgentThread bestHelper = helpRequest.findBestHelper(listeners);
        helpRequest.chooseHelper(bestHelper);
    }
}
