package architecture;

import board.BoardReader;
import board.Goal;
import board.Level;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.io.*;
import java.util.logging.Logger;

public class ClientManager {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ClientManager.class.getSimpleName());

    private FibonacciHeap<Goal> subGoals;
    private LevelManager levelManager;
    private ActionSenderThread actionSenderThread;
    private int numberOfAgents;
    private static ClientManager instance;

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
        this.subGoals = new FibonacciHeap<>();

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
        this.actionSenderThread.run();

        // Instantiate and launch agent threads
        this.levelManager.getLevel().getAgents().forEach(agent -> new AgentThread(agent).run());
    }

    public LevelManager getLevelManager() {
        return this.levelManager;
    }

    public int getNumberOfAgents() {
        return this.numberOfAgents;
    }

    public ActionSenderThread getActionSenderThread() {
        return this.actionSenderThread;
    }
}
