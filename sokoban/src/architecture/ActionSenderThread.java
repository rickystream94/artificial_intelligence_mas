package architecture;

import board.Agent;
import logging.ConsoleLogger;
import planning.actions.PrimitiveTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionSenderThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ActionSenderThread.class.getSimpleName());

    private int numberOfAgents;
    private BlockingQueue<SendActionEvent> eventsCollector;
    private BlockingQueue<SendActionEvent> eventsOrdered;
    private BufferedReader serverInMessages;
    private BufferedWriter serverOutMessages;
    private HashMap<Character, SendActionEvent> currentActions;
    private LevelManager levelManager;

    ActionSenderThread(int numberOfAgents, BufferedReader serverInMessages, BufferedWriter serverOutMessages) {
        this.numberOfAgents = numberOfAgents;
        this.eventsCollector = new ArrayBlockingQueue<>(numberOfAgents);
        this.eventsOrdered = new PriorityBlockingQueue<>(numberOfAgents, new SendActionEventComparator());
        this.serverInMessages = serverInMessages;
        this.serverOutMessages = serverOutMessages;
        this.currentActions = new HashMap<>();
        this.levelManager = ClientManager.getInstance().getLevelManager();
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
                    SendActionEvent sendActionEvent = this.eventsOrdered.take();
                    jointAction.add(sendActionEvent.getAction().toString());
                    this.currentActions.put(sendActionEvent.getAgent().getAgentId(), sendActionEvent);
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

    public void addPrimitiveAction(PrimitiveTask action, Agent agent) {
        this.eventsCollector.add(new SendActionEvent(action, agent));

        if (this.eventsCollector.size() == numberOfAgents) {
            // Drain elements to priority queue (they will be automatically sorted by agentID once inserted)
            this.eventsCollector.drainTo(this.eventsOrdered);
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
        //this.serverOutMessages.write(jointAction);
        //this.serverOutMessages.flush();
        System.out.println(jointAction);
        System.out.flush();

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
