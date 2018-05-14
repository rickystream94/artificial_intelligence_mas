package architecture.conflicts;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.fipa.ClearCellRequest;
import architecture.fipa.HelpRequest;
import architecture.fipa.PerformativeManager;
import logging.ConsoleLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class GlobalConflictResolver implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(GlobalConflictResolver.class.getSimpleName());
    private static GlobalConflictResolver instance;

    private ArrayBlockingQueue<Conflict> conflicts;
    private LevelManager levelManager;

    private GlobalConflictResolver() {
        this.conflicts = new ArrayBlockingQueue<>(ClientManager.getInstance().getNumberOfAgents() * 2);
        this.levelManager = ClientManager.getInstance().getLevelManager();
    }

    public static synchronized GlobalConflictResolver getInstance() {
        if (instance == null)
            instance = new GlobalConflictResolver();
        return instance;
    }

    @Override
    public void run() {
        while (!levelManager.isLevelSolved()) {
            try {
                // Get next available conflict, or wait for it
                Conflict conflict = this.conflicts.take();
                ConsoleLogger.logInfo(LOGGER, String.format("Starting to process conflict between agent %c and %c...", conflict.getCaller().getAgent().getAgentId(), conflict.getBlockingAgent().getAgentId()));

                // Create responses to be sent to the caller agent thread as a notification of successful conflict processing
                ConflictResponse conflictResponse = processConflict(conflict);
                conflict.getCaller().getConflictResponseGatherer().addConflictResponse(conflictResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Registers a new conflict in the queue.
     * If the conflict is not yet in the queue: first agent is reporting the conflict, ignore and wait for second agent.
     * If the conflict is already in the queue: second agent is reporting the conflict --> ready to process and send response
     *
     * @param conflict conflict to be registered
     */
    public synchronized void registerConflict(Conflict conflict) {
        ConsoleLogger.logInfo(LOGGER, String.format("Conflict raised by agent %c", conflict.getCaller().getAgent().getAgentId()));
        try {
            this.conflicts.put(conflict);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * It will decide who is the master and who is the slave. For the slave, a help request will be created and dispatched.
     *
     * @param conflict
     * @return
     */
    private ConflictResponse processConflict(Conflict conflict) {
        // TODO: is not enough to give priority only based on the agent ID: when multiple conflicts are chained, there
        // should be a more strict prioritization (e.g. the agent with lowest priority might be stuck between multiple agents themselves stuck and waiting for help
        boolean isMaster = Character.compare(conflict.getCaller().getAgent().getAgentId(), conflict.getBlockingAgent().getAgentId()) < 0;
        ConflictResponse conflictResponse = new ConflictResponse(isMaster);

        // Create help request for slave and dispatch it
        if (isMaster) {
            HelpRequest helpRequest = new ClearCellRequest(conflict.getCaller(), conflict.getBlockingAgent());
            PerformativeManager.getDefault().dispatchPerformative(helpRequest);
        }

        return conflictResponse;
    }
}
