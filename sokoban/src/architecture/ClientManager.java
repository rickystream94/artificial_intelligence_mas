package architecture;

import architecture.bdi.BDIManager;
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
    private int numberOfAgents;
    private static ClientManager instance;
    private BDIManager bdiManager;

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
        ActionSenderThread actionSenderThread = ActionSenderThread.getInstance();
        actionSenderThread.init(this.numberOfAgents, serverInMessages, serverOutMessages);

        new Thread(actionSenderThread).start();

        /* TODO: next step --> goal desires generation:
        Distinction between DESIRES and INTENTIONS generation:

        ** DESIRES (centralized, triggered by ClientManager before AgentThreads start) **
        The centralized desires generation generates the GOAL DESIRES:
        a goal desire represents a (sub)mission of an agent: they store information about which box goes to which goal
        First step --> Box-Goal assignment (random? simple heuristic? ...) --> Desire object generated
        Second step --> Match between such desires with the agent(s)
        We distinguish between two situations:
        1) Number of agents of same color == 1 --> all goal desires assigned to the same agent
        2) Number of agents of same color > 1 --> (ideally) optimized desire/agent matching, such that the following criteria are satisfied:
            - Each agent will have to achieve an (almost) equal number of desires (e.g. 3 agents, 3 desires --> 1 desire each. Attention: 1+ agents might be left out, if Num_agents>>>Num_desires!)
            - (OPTIONAL) Each agent is assigned desire(s) such that the overall cost to achieve them is minimized
            (this is a non-trivial assignment problem, admitting scenarios with multiple jobs (desires) per agent.
            Since this optimization is not crucial, the trivial implementation of the desire/agent matching is a RANDOM matching

        ** INTENTIONS (step inside agent control loop) **
        Since the desires can't change (boxes/goals don't disappear from the board), each agent will only have to PRIORITIZE which desire it's currently willing to achieve (each loop iteration? Or at less frequent intervals? ...)
        An INTENTION is something more concrete, which shows how the agent is currently trying to achieve that desire
        (e.g. SolveGoal, SolveConflict, ClearPath, MoveToBox, MoveBoxToGoal --> CompoundTask!)
        Intentions are generated for each agent control loop iteration --> deliberation step
         */
        this.bdiManager = new BDIManager();
        this.bdiManager.generateActionsByAgent();

        // Instantiate and launch agent threads
        this.levelManager.getLevel().getAgents().forEach(agent -> new Thread(new AgentThread(agent, desires)).start());
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
