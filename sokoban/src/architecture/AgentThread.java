package architecture;

import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.fipa.Performative;
import architecture.fipa.PerformativeHelpWithBox;
import architecture.fipa.PerformativeManager;
import board.Agent;
import board.Box;
import board.Coordinate;
import board.SokobanObject;
import exceptions.InvalidActionException;
import exceptions.PlanNotFoundException;
import logging.ConsoleLogger;
import planning.HTNPlanner;
import planning.HTNWorldState;
import planning.PrimitivePlan;
import planning.actions.Direction;
import planning.actions.PrimitiveTask;
import planning.relaxations.Relaxation;
import planning.relaxations.RelaxationFactory;
import utils.FibonacciHeap;
import utils.HashMapHelper;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());
    private static int FAILURES_THRESHOLD = 1;

    private Agent agent;
    private FibonacciHeap<Desire> desires;
    private ActionSenderThread actionSenderThread;
    private BlockingQueue<ResponseEvent> responseEvents;
    private LevelManager levelManager;
    private AgentThreadStatus status;
    private int planFailureCounter;
    private Queue<Performative> helpRequests; // TODO: will be used ideally by PerformativeManager to deliver performative events to the agents

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.status = AgentThreadStatus.FREE;
        this.helpRequests = new ConcurrentLinkedDeque<>();
        this.planFailureCounter = 0;
    }

    @Override
    public void run() {
        try {
            while (!this.levelManager.isLevelSolved()) {
            /* TODO: ** INTENTIONS AND DESIRES **
        Since the desires can't change (boxes/goals don't disappear from the board), each agent will only have to PRIORITIZE which desire it's currently willing to achieve (each loop iteration? Or at less frequent intervals? ...)
        An INTENTION is something more concrete, which shows how the agent is currently trying to achieve that desire
        (e.g. SolveGoal, SolveConflict, ClearPath, MoveToBox, MoveBoxToGoal --> CompoundTask!)
        Intentions are generated for each agent control loop iteration --> deliberation step */
                while (!this.desires.isEmpty()) {
                    FibonacciHeap.Entry<Desire> entry = this.desires.dequeueMin();
                    // Get next desire
                    // TODO: a further check when prioritizing should be performed: there are desires that can't be achieved (box is stuck)
                    // We must check for goals that should be achieved in a specific order --> we need a new CompoundTask type like ClearBox
                    // that should move a blocking box somewhere close to the edges to free the way for the targeted box
                    // TODO: desires re-prioritization to be performed and invoked during planning! e.g. if we haven't achieved our goal
                    // after 5/10 primitive actions return the final plan, re-calculate the priorities and re-evaluate the desires:
                    // there might be some desires now that have less priority than before and the agent will commit to them first
                    // TODO: when there are more goals of same type, agent should prioritize the desires that fulfill
                    // the goals that are at the edges (to avoid blocking other boxes) (example from level SAsokobanLevel96)
                    Desire desire = entry.getValue();
                    this.agent.setCurrentTargetBox(desire.getBox());
                    ConsoleLogger.logInfo(LOGGER, "Agent " + this.agent.getAgentId() + " committing to desire " + desire);

                    // Get percepts
                    Relaxation planningRelaxation = RelaxationFactory.getBestPlanningRelaxation(this.agent.getColor(), desire, this.planFailureCounter);
                    HTNWorldState worldState = new HTNWorldState(this.agent, desire, planningRelaxation);

                    try {
                        // Plan
                        HTNPlanner planner = new HTNPlanner(worldState, desire);
                        PrimitivePlan plan = planner.findPlan();
                        this.planFailureCounter = 0; // Reset failure counter when a plan is successfully found
                        executePlan(plan);
                    } catch (InvalidActionException e) {
                        ConsoleLogger.logInfo(LOGGER, e.getMessage());
                        // Current desire wasn't achieved --> add it back to the heap!
                        this.desires.enqueue(entry.getValue(), entry.getPriority());
                        detectBlockingObject(e.getFailedAction(), entry.getValue());
                    } catch (PlanNotFoundException e) {
                        this.planFailureCounter++;
                        if (this.planFailureCounter == 1) {
                            // Current desire wasn't achieved --> add it back to the heap!
                            // First failed attempt allowed, will switch to new relaxation
                            ConsoleLogger.logInfo(LOGGER, e.getMessage());
                            this.desires.enqueue(entry.getValue(), entry.getPriority());
                        } else {
                            // Planning keeps failing, unexpected exception. Throw and quit.
                            ConsoleLogger.logError(LOGGER, e.getMessage());
                            System.exit(1);
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
        int failedAttempts = 0;
        do {
            if (failedAttempts == FAILURES_THRESHOLD)
                throw new InvalidActionException(this.agent.getAgentId(), tasks.peek());
            status = AgentThreadStatus.BUSY;
            this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
            try {
                if (getServerResponse()) {
                    tasks.remove();
                    failedAttempts = 0;
                } else {
                    failedAttempts++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!tasks.isEmpty());
        status = AgentThreadStatus.FREE; // TODO: must be placed properly where relevant
    }

    /**
     * This method is in charge of letting the agent figure out whether he can solve the blocking issue on its own or
     * he needs to ask for help
     *
     * @param failedAction last action that failed
     * @param desire       current desire to achieve
     */
    private void detectBlockingObject(PrimitiveTask failedAction, Desire desire) {
        // TODO Here's there's an issue: we're only detecting boxes that block the AGENT and not the BOX
        // This way we would consider a blocking-box the same one we're trying to put in the goal, resulting in
        // infinite loop
        Coordinate blockingCell;
        switch (failedAction.getType()) {
            case Move:
                blockingCell = Direction.getPositionByDirection(this.agent.getCoordinate(), failedAction.getDir1());
                break;
            case Push:
                Box box = desire.getBox();
                blockingCell = Direction.getPositionByDirection(box.getCoordinate(), failedAction.getDir2());
                break;
            case Pull:
                blockingCell = Direction.getPositionByDirection(this.agent.getCoordinate(), failedAction.getDir1());
                break;
            default:
                blockingCell = null; // Unreachable
        }
        SokobanObject blockingObject = this.levelManager.getLevel().dynamicObjectAt(Objects.requireNonNull(blockingCell));

        // Examine blocking object
        if (blockingObject instanceof Agent) {
            // TODO: decide how to behave in either case: the blocking agent's color doesn't matter, in either case it needs to free the cell
        } else if (blockingObject instanceof Box) {
            if (((Box) blockingObject).getColor() == this.agent.getColor()) {
                // Blocking box is of the same color
                List<Coordinate> potentialNewPositions = Coordinate.getEmptyCellsWithFixedDistanceFrom(blockingObject.getCoordinate(), 1);
                potentialNewPositions.add(this.agent.getCoordinate());
                Map<Object, Integer> distances = new HashMap<>();
                potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, desire.getTarget())));
                Coordinate chosenTarget = (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);

                // Create new desire and enqueue it with maximum priority
                ClearPathDesire clearPathDesire = new ClearPathDesire((Box) blockingObject, chosenTarget);
                this.desires.enqueue(clearPathDesire, -1000);
            } else {
                // Box of different color
                // TODO: need for help! This box is blocking me and I can't move it

                // Set agent's status to stuck: will re-plan accordingly in next iteration --> needs help!
                this.status = AgentThreadStatus.STUCK;

                // The Performative Message can be a Request, Proposal or Inquirie (I dont think we need this)
                // In here the agent has to figure out why he is stuck and determine the how he needs help
                // Create the message and dispatch it on the Bus
                Performative performative = new PerformativeHelpWithBox(null, this);
                PerformativeManager.getDefault().execute(performative);
            }
        }
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
