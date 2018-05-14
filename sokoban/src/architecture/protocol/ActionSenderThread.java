package architecture.protocol;

import architecture.ClientManager;
import architecture.LevelManager;
import board.Agent;
import logging.ConsoleLogger;
import planning.actions.PrimitiveTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionSenderThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ActionSenderThread.class.getSimpleName());
    private static final Object lock = new Object();

    private int numberOfAgents;
    private int jointActionsSent;
    private Queue<SendActionEvent> eventsOrdered;
    private BufferedReader serverInMessages;
    private BufferedWriter serverOutMessages;
    private HashMap<Character, SendActionEvent> currentActions;
    private LevelManager levelManager;

    public ActionSenderThread(int numberOfAgents, BufferedReader serverInMessages, BufferedWriter serverOutMessages) {
        this.numberOfAgents = numberOfAgents;
        this.eventsOrdered = new PriorityQueue<>(numberOfAgents, new SendActionEventComparator());
        this.serverInMessages = serverInMessages;
        this.serverOutMessages = serverOutMessages;
        this.currentActions = new HashMap<>();
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.jointActionsSent = 0;
    }

    @Override
    public void run() {
        StringJoiner jointAction;

        // Each loop iteration represents a turn
        // By the end of the current iteration, a single joint action is sent to the server
        while (true) {
            // Wait for all agent threads to push their actions in the queue
            synchronized (lock) {
                while (this.eventsOrdered.size() != this.numberOfAgents) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            // All agents have sent their actions: build joint action
            jointAction = new StringJoiner(",", "[", "]");
            while (!this.eventsOrdered.isEmpty()) {
                SendActionEvent sendActionEvent = this.eventsOrdered.remove();
                jointAction.add(sendActionEvent.getAction().toString());
                this.currentActions.put(sendActionEvent.getAgent().getAgentId(), sendActionEvent);
            }
            // At this point, every agent has put its action in the queue
            // Send joint action to server and process response
            try {
                String response = sendToServer(jointAction.toString());
                jointActionsSent++;
                ConsoleLogger.logInfo(LOGGER, String.format("*** SENT ACTION #%d: %s TO SERVER ***", jointActionsSent, jointAction.toString()));
                processResponse(response);
            } catch (IOException e) {
                ConsoleLogger.logError(LOGGER, e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void addPrimitiveAction(PrimitiveTask action, Agent agent) {
        synchronized (lock) {
            this.eventsOrdered.add(new SendActionEvent(action, agent));
            lock.notify();
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
        this.serverOutMessages.write(jointAction + "\n");
        this.serverOutMessages.flush();

        String response;
        do {
            response = this.serverInMessages.readLine();
        } while (response.isEmpty());
        return response;
    }

    private void processResponse(String response) {
        // Create list of ResponseEvent such that each element maps the agent id with the correct response
        String[] stringResponses = response.replaceAll("[\\[\\]]", "").split(", ");
        List<ResponseEvent> responseEvents = IntStream.range(0, stringResponses.length)
                .mapToObj(i -> new ResponseEvent(Character.forDigit(i, 10), Boolean.parseBoolean(stringResponses[i])))
                .collect(Collectors.toList());

        responseEvents.stream().filter(ResponseEvent::isActionSuccessful)
                .forEach(r -> {
                    SendActionEvent sendActionEvent = this.currentActions.get(r.getAgentId());
                    levelManager.applyAction(sendActionEvent.getAction(), sendActionEvent.getAgent());
                });

        // Dispatch results from server to agent threads
        EventBus.getDefault().dispatch(responseEvents);
    }
}
