package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.BDIManager;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
import architecture.fipa.HelpRequestResolver;
import architecture.protocol.ActionSenderThread;
import architecture.protocol.ResponseEvent;
import board.Agent;
import board.AgentStatus;
import exceptions.*;
import logging.ConsoleLogger;
import planning.HTNPlanner;
import planning.HTNWorldState;
import planning.PrimitivePlan;
import planning.actions.PrimitiveTask;
import planning.relaxations.Relaxation;
import planning.relaxations.RelaxationFactory;
import utils.FibonacciHeap;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    private HelpRequestResolver helpRequestResolver;

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.agent.setStatus(AgentStatus.WORKING);
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.lockDetector = new LockDetector(agent);
        this.desireHelper = new DesireHelper(agent);
        this.helpRequestResolver = new HelpRequestResolver(agent);
    }

    @Override
    public void run() {
        try {
            while (!this.levelManager.isLevelSolved()) {
                while (!this.desires.isEmpty() || this.helpRequestResolver.hasRequestsToProcess()) {
                    // TODO: BDIManager's re-prioritization should be re-used here:
                    // Where to place it? --> before de-queueing next desire
                    // What's the reason for this? --> after X successful actions executed during executePlan
                    // there might be some desires now that have less priority than before
                    // and the agent will commit to them first
                    // How? --> Break plan execution, re-enqueue un-achieved desire and continue
                    // TODO: desires to be prioritized according to strict order (tunnels or dead-ends)

                    // If some previously solved goals are now unsolved (because the box has been cleared), re-enqueue them!
                    this.desireHelper.checkAndEnqueueUnsolvedGoalDesires(this.desires);

                    // Check if there are any help requests I should commit to
                    if (this.helpRequestResolver.hasRequestsToProcess()) {
                        this.helpRequestResolver.processHelpRequest(this.lockDetector);
                    }

                    // Get next desire
                    Desire desire;
                    try {
                        desire = this.desireHelper.getNextDesire(this.desires, lockDetector);
                    } catch (NoProgressException e) {
                        ConsoleLogger.logInfo(LOGGER, e.getMessage());
                        freshRestart();
                        //lockDetector.incrementMaxNoProgressCounter();
                        continue;
                    } catch (NoSuchElementException e) {
                        break;
                    }

                    // Desire is valid: proceed to prepare and execute planning
                    if (desire.getBox() != null)
                        this.agent.setCurrentTargetBox(desire.getBox());
                    ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: committing to desire %s", this.agent.getAgentId(), desire));

                    // Get percepts
                    Relaxation planningRelaxation = RelaxationFactory.getBestPlanningRelaxation(this.agent, this.lockDetector.getNumFailedPlans());
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
                        // Action was rejected by server
                        ConsoleLogger.logInfo(LOGGER, e.getMessage());
                        // Current desire wasn't achieved --> add it back to the heap!
                        if (desire instanceof GoalDesire)
                            this.desires.enqueue(desire, desireHelper.getCurrentDesirePriority());
                        try {
                            this.lockDetector.detectBlockingObject(e.getFailedAction(), desire);
                        } catch (NoProgressException ex) {
                            // Agent is experiencing a deadlock among ClearPathDesires --> Cleanup and Re-prioritize desires!
                            ConsoleLogger.logInfo(LOGGER, ex.getMessage());
                            //lockDetector.incrementMaxNoProgressCounter();
                            freshRestart();
                        } catch (StuckByForeignBoxException | StuckByAgentException ex) {
                            helpRequestResolver.askForHelp(this, ex);
                        }
                    } catch (PlanNotFoundException e) {
                        // Current desire wasn't achieved --> add it back to the heap!
                        this.lockDetector.planFailed();
                        if (this.lockDetector.shouldChangeRelaxation()) {
                            if (desire instanceof GoalDesire)
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

    private void freshRestart() {
        //lockDetector.restoreClearingDistancesForAllObjects();
        //lockDetector.clearChosenTargetsForAllObjects();
        lockDetector.clearBlockingObjects();
        this.desires = BDIManager.recomputeDesiresForAgent(agent, this.desires);
    }

    private void executePlan(PrimitivePlan plan) throws InvalidActionException {
        Queue<PrimitiveTask> tasks = plan.getTasks();
        this.lockDetector.resetFailedActions();
        try {
            if (tasks.isEmpty()) {
                sendNoOp();
                getServerResponse();
                return;
            }
            do {
                if (this.lockDetector.isStuck())
                    throw new InvalidActionException(this.agent.getAgentId(), tasks.peek());
                this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
                if (getServerResponse()) {
                    tasks.remove();
                    this.lockDetector.resetFailedActions();
                    // If the agent was stuck and the action is successful, set back to WORKING state
                    this.agent.setStatus(AgentStatus.WORKING);
                } else {
                    this.lockDetector.actionFailed();
                }
            } while (!tasks.isEmpty());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void idle() throws InterruptedException {
        if (this.agent.getStatus() == AgentStatus.WORKING) {
            this.agent.setStatus(AgentStatus.FREE);
            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: WORK DONE!", this.agent.getAgentId()));
        }
        this.desireHelper.checkAndEnqueueUnsolvedGoalDesires(this.desires);
        if (!this.helpRequestResolver.hasRequestsToProcess()) {
            sendNoOp();
            getServerResponse();
        } else {
            this.agent.setStatus(AgentStatus.WORKING);
            this.helpRequestResolver.processHelpRequest(this.lockDetector);
        }
    }

    private void sendNoOp() {
        this.actionSenderThread.addPrimitiveAction(new PrimitiveTask(), this.agent);
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

    public HelpRequestResolver getHelpRequestResolver() {
        return helpRequestResolver;
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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof AgentThread))
            return false;
        AgentThread other = (AgentThread) o;
        return other.agent.equals(this.agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.agent);
    }
}
