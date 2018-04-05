package architecture;

import board.BoardReader;
import board.Goal;
import board.Level;
import logging.ConsoleLogger;
import utils.FibonacciHeap;

import java.io.*;
import java.util.logging.Logger;

public class ClientManager implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(ClientManager.class.getSimpleName());

    private BufferedReader serverInputMessages;
    private BufferedWriter serverOutputMessages;
    private FibonacciHeap<Goal> subGoals;
    private LevelManager levelManager;
    private ActionSenderThread actionSenderThread;

    @Override
    public void run() {
        this.serverInputMessages = new BufferedReader(new InputStreamReader(System.in));
        this.serverOutputMessages = new BufferedWriter(new OutputStreamWriter(System.out));
        this.subGoals = new FibonacciHeap<>();

        // Read and create level
        Level level = null;
        try {
            level = BoardReader.readLevel(serverInputMessages);
        } catch (IOException e) {
            ConsoleLogger.logError(LOGGER, e.getMessage());
            System.exit(1);
        }

        // Instantiate LevelManager
        this.levelManager = LevelManager.getInstance(level);

        // Instantiate ActionSenderThread
        this.actionSenderThread = new ActionSenderThread(this.levelManager.getLevel().getAgents().size(), serverInputMessages, serverOutputMessages);
        this.actionSenderThread.run();
    }
}
