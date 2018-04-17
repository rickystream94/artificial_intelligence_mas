package architecture;

import architecture.bdi.BDIManager;
import architecture.bdi.Desire;
import board.Agent;
import board.BoardReader;
import board.Level;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClientManager {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ClientManager.class.getSimpleName());

    private LevelManager levelManager;
    private int numberOfAgents;
    private static ClientManager instance;
    private BDIManager bdiManager;
    private ActionSenderThread actionSenderThread;

    private ClientManager() {
    }

    public static ClientManager getInstance() {
        if (instance == null)
            instance = new ClientManager();
        return instance;
    }

    public void init() {
        BufferedReader serverInMessages = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter serverOutMessages = new BufferedWriter(new OutputStreamWriter(System.out));

        // Read and create level
        Level level = null;
        try {
            level = BoardReader.readLevel(serverInMessages);
        } catch (IOException e) {
            ConsoleLogger.logError(LOGGER, e.getMessage());
            System.exit(1);
        }

        // Instantiate LevelManager
        this.levelManager = new LevelManager(level);
        this.numberOfAgents = this.levelManager.getLevel().getAgents().size();

        // Instantiate and launch ActionSenderThread
        this.actionSenderThread = new ActionSenderThread(this.numberOfAgents, serverInMessages, serverOutMessages);
        new Thread(actionSenderThread).start();

        // Instantiate BDI Manager and computes desires
        this.bdiManager = new BDIManager();
        Map<Agent, FibonacciHeap<Desire>> desires = this.bdiManager.generateDesires();

        // Instantiate agent threads, add them as subscribers to ActionSenderThread (Publisher) and launch them
        List<AgentThread> agentThreads = this.levelManager.getLevel().getAgents().stream().map(agent -> new AgentThread(agent, desires.get(agent))).collect(Collectors.toList());
        this.actionSenderThread.addSubscribers(agentThreads);
        agentThreads.forEach(agentThread -> new Thread(agentThread).start());
    }

    public LevelManager getLevelManager() {
        return this.levelManager;
    }

    public int getNumberOfAgents() {
        return this.numberOfAgents;
    }

    public ActionSenderThread getActionSenderThread() {
        return actionSenderThread;
    }
}
