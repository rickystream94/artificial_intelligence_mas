package architecture;

import planning.PrimitiveAction;
import logging.ConsoleLogger;
import planning.PrimitiveActionComparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

public class ActionSenderThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ActionSenderThread.class.getSimpleName());

    private int numberOfAgents;
    private BlockingQueue<PrimitiveAction> primitiveActionsCollector;
    private BlockingQueue<PrimitiveAction> primitiveActionsOrdered;
    private BufferedReader serverInMessages;
    private BufferedWriter serverOutMessages;

    public ActionSenderThread(int numberOfAgents, BufferedReader serverInMessages, BufferedWriter serverOutMessages) {
        this.numberOfAgents = numberOfAgents;
        this.primitiveActionsCollector = new ArrayBlockingQueue<>(numberOfAgents);
        this.primitiveActionsOrdered = new PriorityBlockingQueue<>(numberOfAgents, new PrimitiveActionComparator());
        this.serverInMessages = serverInMessages;
        this.serverOutMessages = serverOutMessages;
    }

    @Override
    public void run() {
        StringJoiner jointAction;
        int polledActions;

        // Each loop iteration represents a turn
        // By the end of the current iteration, a single joint action is sent to the server
        while (true) {
            jointAction = new StringJoiner(",", "[", "]");
            polledActions = 0;
            while (polledActions != this.numberOfAgents) {
                try {
                    // take() will wait until an element becomes available in the queue
                    PrimitiveAction agentAction = this.primitiveActionsOrdered.take();
                    jointAction.add(agentAction.toString());
                    polledActions++;
                } catch (InterruptedException e) {
                    ConsoleLogger.logError(LOGGER, e.getMessage());
                }
            }
            // At this point, every agent has put its action in the queue
            // Send joint action to server and process response
            try {
                String response = sendToServer(jointAction.toString());
                processResponse(response);
            } catch (IOException e) {
                ConsoleLogger.logError(LOGGER, e.getMessage());
                System.exit(1);
            }
        }
    }

    public void addPrimitiveAction(PrimitiveAction action) {
        this.primitiveActionsCollector.add(action);

        if (this.primitiveActionsCollector.size() == numberOfAgents) {
            // Drain elements to priority queue (they will be automatically sorted by agentID once inserted)
            this.primitiveActionsCollector.drainTo(this.primitiveActionsOrdered);
        }
    }

    /**
     * Sends a joint action to the server and returns the corresponding response
     *
     * @param jointAction the joint action to be sent
     * @return response from the server
     * @throws IOException
     */
    private String sendToServer(String jointAction) throws IOException {
        this.serverOutMessages.write(jointAction);
        this.serverOutMessages.flush();

        return this.serverInMessages.readLine();
    }

    private void processResponse(String response) {
        String[] stringResponses = response.replaceAll("[\\[\\]]", "").split(",");
        Boolean[] responses = Arrays.stream(stringResponses).map(Boolean::parseBoolean).toArray(Boolean[]::new);

        // TODO: perform changes to the level with the support of LevelManager
        LevelManager levelManager = ClientManager.getInstance().getLevelManager();
    }
}
