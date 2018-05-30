package architecture.agent;

import architecture.ClientManager;
import architecture.LevelManager;
import architecture.bdi.BDIManager;
import architecture.bdi.ClearBoxDesire;
import architecture.bdi.ClearCellDesire;
import architecture.bdi.Desire;
import architecture.conflicts.ConflictResponseGatherer;
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

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());

    private Agent agent;
    private ActionSenderThread actionSenderThread;
    private BlockingQueue<ResponseEvent> responseEvents;
    private LevelManager levelManager;
    private LockDetector lockDetector;
    private DesireHelper desireHelper;
    private HelpRequestResolver helpRequestResolver;
    private PlanningHelper planningHelper;
    private ActionHelper actionHelper;
    private ConflictResponseGatherer conflictResponseGatherer;
    private BDIManager bdiManager;

    public AgentThread(Agent agent) {
        this.agent = agent;
        this.agent.setStatus(AgentStatus.WORKING);
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.lockDetector = new LockDetector(agent);
        this.desireHelper = new DesireHelper(agent);
        this.helpRequestResolver = new HelpRequestResolver(agent);
        this.planningHelper = new PlanningHelper(agent);
        this.actionHelper = new ActionHelper(agent);
        this.conflictResponseGatherer = new ConflictResponseGatherer();
        this.bdiManager = ClientManager.getInstance().getBdiManager();
    }

    @Override
    public void run() {
        try {
            while (!this.levelManager.isLevelSolved()) {
                // Check if there are any help requests I should commit to
                if (this.helpRequestResolver.hasRequestsToProcess())
                    this.helpRequestResolver.processHelpRequest(this.lockDetector);

                // Get next desire
                Desire desire;
                try {
                    desire = this.desireHelper.getNextDesire(lockDetector);
                } catch (NoAvailableTargetsException e) {
                    ConsoleLogger.logInfo(LOGGER, e.getMessage());
                    if (lockDetector.getClearingDistance(e.getBlockingObject()) < PlanningHelper.MAX_CLEARING_DISTANCE)
                        lockDetector.incrementClearingDistance(e.getBlockingObject());
                    else {
                        lockDetector.restoreClearingDistanceForObject(e.getBlockingObject());
                        this.planningHelper.noMoreTargets();
                    }
                    lockDetector.clearChosenTargetsForObject(e.getBlockingObject());
                    continue;
                } catch (NoAvailableBoxesException | NoAvailableGoalsException | NotLowestPriorityGoalException e) {
                    ConsoleLogger.logInfo(LOGGER, e.getMessage());
                    idle();
                    continue;
                }

                // Desire is valid: proceed to prepare and execute planning
                if (desire.getBox() != null)
                    this.agent.setCurrentTargetBox(desire.getBox());
                ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: committing to desire %s", this.agent.getAgentId(), desire));

                // Get percepts
                Relaxation planningRelaxation = RelaxationFactory.getBestPlanningRelaxation(this.agent, this.planningHelper.getNumFailedPlans());
                HTNWorldState worldState = new HTNWorldState(this.agent, desire, planningRelaxation);

                try {
                    // Plan
                    HTNPlanner planner = new HTNPlanner(worldState, desire);
                    PrimitivePlan plan = planner.findPlan();
                    this.planningHelper.planSuccessful(); // Reset failure counter when a plan is successfully found
                    executePlan(plan);

                    // If we reach this point, the desire is achieved. If goal desire, back it up
                    this.desireHelper.achievedDesire(this.lockDetector);
                } catch (InvalidActionException e) {
                    // Action was rejected by server
                    this.bdiManager.resetAgentDesire(agent);
                    ConsoleLogger.logInfo(LOGGER, e.getMessage());
                    try {
                        this.lockDetector.detectBlockingObject(e.getFailedAction(), desire);
                    } catch (NoProgressException ex) {
                        // Agent is experiencing a deadlock among ClearPathDesires --> Cleanup and Re-prioritize desires!
                        ConsoleLogger.logInfo(LOGGER, ex.getMessage());
                        freshRestart();
                    } catch (StuckByForeignBoxException | StuckByAgentException ex) {
                        helpRequestResolver.askForHelp(this, ex, desire, lockDetector);
                    }
                } catch (PlanNotFoundException e) {
                    this.bdiManager.resetAgentDesire(agent);
                    this.planningHelper.planFailed(desire, lockDetector);
                    if (this.planningHelper.canChangeRelaxation()) {
                        ConsoleLogger.logInfo(LOGGER, e.getMessage());
                    } else {
                        // Plan keeps failing --> A fake empty cell has been set as target, remove it!
                        if (desire instanceof ClearCellDesire || desire instanceof ClearBoxDesire) {
                            ConsoleLogger.logInfo(LOGGER, String.format("Agent %c: removed fake empty cell %s", agent.getAgentId(), desire.getTarget()));
                            levelManager.getLevel().removeEmptyCell(desire.getTarget());
                        } else {
                            ConsoleLogger.logError(LOGGER, "Couldn't find a plan!");
                            System.exit(1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ConsoleLogger.logError(LOGGER, e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void freshRestart() {
        lockDetector.clearBlockingObjects();
    }

    private void executePlan(PrimitivePlan plan) throws InvalidActionException {
        Queue<PrimitiveTask> tasks = plan.getTasks();
        this.actionHelper.resetFailedActions();
        try {
            if (tasks.isEmpty()) {
                sendNoOp();
                getServerResponse();
                return;
            }
            do {
                // Break plan execution if the current desire is already achieved (maybe by another agent?)
                if (this.levelManager.getLevel().isDesireAchieved(this.desireHelper.getCurrentDesire()))
                    return;
                if (this.actionHelper.isStuck())
                    throw new InvalidActionException(this.agent.getAgentId(), tasks.peek());
                this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
                if (getServerResponse()) {
                    tasks.remove();
                    this.actionHelper.resetFailedActions();
                    // If the agent was stuck and the action is successful, set back to WORKING state
                    this.agent.setStatus(AgentStatus.WORKING);
                } else {
                    this.actionHelper.actionFailed();
                }
            } while (!tasks.isEmpty());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * While a thread is in idle, it will still check for unsolved goal desires and help requests to process
     *
     * @throws InterruptedException
     */
    private void idle() throws InterruptedException {
        if (this.agent.getStatus() == AgentStatus.WORKING) {
            this.agent.setStatus(AgentStatus.FREE);
        }
        sendNoOp();
        getServerResponse();
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

    public ConflictResponseGatherer getConflictResponseGatherer() {
        return this.conflictResponseGatherer;
    }

    @Override
    public String toString() {
        return String.format("Agent %c ", this.agent.getAgentId());
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
