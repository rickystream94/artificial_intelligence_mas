package architecture;

import architecture.bdi.BDIManager;
import architecture.bdi.Desire;
import architecture.fipa.Performative;
import architecture.fipa.PerformativeHelpWithBox;
import architecture.fipa.PerformativeManager;
import board.Agent;
import exceptions.InvalidActionException;
import exceptions.NoProgressException;
import exceptions.PlanNotFoundException;
import exceptions.StuckByForeignBoxException;
import logging.ConsoleLogger;
import planning.HTNPlanner;
import planning.HTNWorldState;
import planning.PrimitivePlan;
import planning.actions.PrimitiveTask;
import planning.relaxations.Relaxation;
import planning.relaxations.RelaxationFactory;
import utils.FibonacciHeap;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());

    private Agent agent;
    private FibonacciHeap<Desire> desires;
    private ActionSenderThread actionSenderThread;
    private BlockingQueue<ResponseEvent> responseEvents;
    private LevelManager levelManager;

    private LockDetector lockDetector;
    private DesireHelper desireHelper;
    private AgentThreadStatus status;
    private Queue<Performative> helpRequests; // TODO: will be used ideally by PerformativeManager to deliver performative events to the agents

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.status = AgentThreadStatus.FREE;
        this.lockDetector = new LockDetector(agent);
        this.desireHelper = new DesireHelper(agent);
        this.helpRequests = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void run() {
        try {
            while (!this.levelManager.isLevelSolved()) {
                while (!this.desires.isEmpty()) {
                    // TODO: BDIManager's re-prioritization should be re-used here:
                    // Where to place it? --> before de-queueing next desire
                    // What's the reason for this? --> after X successful actions executed during executePlan
                    // there might be some desires now that have less priority than before
                    // and the agent will commit to them first
                    // How? --> Break plan execution, re-enqueue un-achieved desire and continue
                    // TODO: desires to be prioritized according to strict order (tunnels or dead-ends)

                    // If some previously solved goals are now unsolved (because the box has been cleared), re-enqueue them!
                    this.desireHelper.checkAndEnqueueUnsolvedGoalDesires(this.desires);

                    // Get next desire
                    Desire desire = this.desireHelper.getNextDesire(this.desires, lockDetector);
                    this.agent.setCurrentTargetBox(desire.getBox());
                    ConsoleLogger.logInfo(LOGGER, "Agent " + this.agent.getAgentId() + " committing to desire " + desire);

                    // Get percepts
                    Relaxation planningRelaxation = RelaxationFactory.getBestPlanningRelaxation(this.agent.getColor(), desire, this.lockDetector.getNumFailedPlans());
                    HTNWorldState worldState = new HTNWorldState(this.agent, desire, planningRelaxation);

                    try {
                        // Plan
                        HTNPlanner planner = new HTNPlanner(worldState, desire);
                        PrimitivePlan plan = planner.findPlan();
                        this.lockDetector.planSuccessful(); // Reset failure counter when a plan is successfully found
                        executePlan(plan);

                        // If we reach this point, the desire is achieved. If goal desire, back it up
                        this.desireHelper.achievedDesire(this.lockDetector);
                    } catch (InvalidActionException e) {
                        ConsoleLogger.logInfo(LOGGER, e.getMessage());
                        // Current desire wasn't achieved --> add it back to the heap!
                        this.desires.enqueue(desire, desireHelper.getCurrentDesirePriority());
                        try {
                            this.lockDetector.detectBlockingObject(e.getFailedAction(), desire, this.desires);
                        } catch (NoProgressException ex) {
                            // Agent is experiencing a deadlock among ClearPathDesires --> Cleanup and Re-prioritize desires!
                            ConsoleLogger.logInfo(LOGGER, e.getMessage());
                            this.desires = BDIManager.recomputeDesiresForAgent(agent, this.desires);
                        } catch (StuckByForeignBoxException ex) {
                            // TODO: needs help! This box is blocking me and I can't move it --> Communication
                            this.status = AgentThreadStatus.STUCK;

                            // The Performative Message can be a Request, Proposal or Inquirie (I dont think we need this)
                            // In here the agent has to figure out why he is stuck and determine the how he needs help
                            // Create the message and dispatch it on the Bus
                            Performative performative = new PerformativeHelpWithBox(null, this);
                            PerformativeManager.getDefault().execute(performative);
                        }
                    } catch (PlanNotFoundException e) {
                        // Current desire wasn't achieved --> add it back to the heap!
                        this.lockDetector.planFailed();
                        if (this.lockDetector.needsReplanning()) {
                            this.desires.enqueue(desireHelper.getCurrentDesire(), desireHelper.getCurrentDesirePriority());
                            // First failed attempt allowed, will switch to new relaxation
                            ConsoleLogger.logInfo(LOGGER, e.getMessage());
                        } else {
                            // Plan keeps failing --> A fake empty cell has been set as target, remove it!
                            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: removed fake empty cell %s", agent.getAgentId(), desire.getTarget()));
                            levelManager.getLevel().removeEmptyCell(desire.getTarget());
                        }
                    }
                }

                // Agent has no more desires --> send NoOP actions
                idle();
            }
        } catch (Exception e) {
            ConsoleLogger.logError(LOGGER, e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void executePlan(PrimitivePlan plan) throws InvalidActionException {
        Queue<PrimitiveTask> tasks = plan.getTasks();
        this.lockDetector.resetFailedActions();
        do {
            if (this.lockDetector.isStuck())
                throw new InvalidActionException(this.agent.getAgentId(), tasks.peek());
            status = AgentThreadStatus.BUSY;
            this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
            try {
                if (getServerResponse()) {
                    tasks.remove();
                    this.lockDetector.resetFailedActions();
                } else {
                    this.lockDetector.actionFailed();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!tasks.isEmpty());
        status = AgentThreadStatus.FREE; // TODO: must be placed properly where relevant
    }

    private void idle() throws InterruptedException {
        setStatus(AgentThreadStatus.FREE);
        if (this.helpRequests.isEmpty()) {
            this.actionSenderThread.addPrimitiveAction(new PrimitiveTask(), this.agent);
            getServerResponse();
        } else {
            setStatus(AgentThreadStatus.HELPING);
            // TODO: should help
        }
    }

    public void sendServerResponse(ResponseEvent responseEvent) {
        assert responseEvent.getAgentId() == this.agent.getAgentId();
        this.responseEvents.add(responseEvent);
    }

    public boolean getServerResponse() throws InterruptedException {
        ResponseEvent responseEvent = this.responseEvents.take();
        return responseEvent.isActionSuccessful();
    }

    public Agent getAgent() {
        return agent;
    }

    /**
     * The status should be queried synchronously
     *
     * @return current Agent's status
     */
    public synchronized AgentThreadStatus getStatus() {
        return this.status;
    }

    private void setStatus(AgentThreadStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<FibonacciHeap.Entry<Desire>> entries = new ArrayList<>();
        sb.append(String.format("Agent %c with desires:\n", this.agent.getAgentId()));
        while (!this.desires.isEmpty()) {
            FibonacciHeap.Entry<Desire> entry = this.desires.dequeueMin();
            sb.append(entry.getValue().toString()).append("\n");
            entries.add(entry);
        }
        entries.forEach(entry -> this.desires.enqueue(entry.getValue(), entry.getPriority()));
        return sb.toString();
    }
}
