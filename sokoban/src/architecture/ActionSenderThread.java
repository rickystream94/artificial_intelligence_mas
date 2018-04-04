package architecture;

import logging.ConsoleLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

public class ActionSenderThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ActionSenderThread.class.getSimpleName());

    private int numberOfAgents;
    private BlockingQueue<AgentAction> agentsActionsOrdered;
    private BlockingQueue<AgentAction> agentActionsCollector;

    public ActionSenderThread(int numberOfAgents) {
        this.numberOfAgents = numberOfAgents;
        this.agentActionsCollector = new ArrayBlockingQueue<>(numberOfAgents);
        this.agentsActionsOrdered = new PriorityBlockingQueue<>(numberOfAgents, new AgentActionComparator());
    }

    @Override
    public void run() {
        while (true) {
            StringBuilder jointAction = new StringBuilder();
            while (!this.agentsActionsOrdered.isEmpty()) {
                try {
                    // take() will wait until an element becomes available in the queue
                    AgentAction agentAction = this.agentsActionsOrdered.take();
                    //TODO: to implement --> form joint action and send it to server
                } catch (InterruptedException e) {
                    ConsoleLogger.logError(LOGGER, e.getMessage());
                }
            }
        }
    }

    public synchronized void addAgentAction(AgentAction action) {
        this.agentActionsCollector.add(action);

        if (this.agentActionsCollector.size() == numberOfAgents) {
            // Drain elements to priority queue (they will be automatically sorted by agentID once inserted)
            this.agentActionsCollector.drainTo(this.agentsActionsOrdered);
        }
    }
}
