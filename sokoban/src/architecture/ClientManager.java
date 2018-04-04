package architecture;

import board.BoardReader;
import board.Goal;
import board.Level;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class ClientManager implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ClientManager.class.getSimpleName());

    private BufferedReader serverMessages;
    private FibonacciHeap<Goal> subGoals;
    private LevelManager levelManager;
    private ActionSenderThread actionSenderThread;

    @Override
    public void run() {
        this.serverMessages = new BufferedReader(new InputStreamReader(System.in));

        this.subGoals = new FibonacciHeap<Goal>();

        // Read and create level
        Level level = null;
        try {
            level = BoardReader.readLevel(serverMessages);
        } catch (IOException e) {
            ConsoleLogger.logError(LOGGER, e.getMessage());
            System.exit(1);
        }

        // Instantiate LevelManager
        this.levelManager = LevelManager.getInstance(level);

        // Instantiate ActionSenderThread
        this.actionSenderThread = new ActionSenderThread(this.levelManager.getLevel().getAgents().size());
        this.actionSenderThread.run();
    }
}
