package architecture;

import architecture.bdi.ClearPathDesire;
import architecture.bdi.Desire;
import architecture.bdi.GoalDesire;
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

    private Agent agent;
    private FibonacciHeap<Desire> desires;
    private Map<GoalDesire, Double> achievedGoalDesiresPriorityMap;
    private ActionSenderThread actionSenderThread;
    private BlockingQueue<ResponseEvent> responseEvents;
    private LevelManager levelManager;
    private LockDetector lockDetector;
    private AgentThreadStatus status;
    private Desire previouslyAchievedDesire;
    private Queue<Performative> helpRequests; // TODO: will be used ideally by PerformativeManager to deliver performative events to the agents

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.achievedGoalDesiresPriorityMap = new HashMap<>();
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.status = AgentThreadStatus.FREE;
        this.lockDetector = new LockDetector();
        this.helpRequests = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void run() {
        try {
            while (!this.levelManager.isLevelSolved()) {
                while (!this.desires.isEmpty()) {
                    // TODO: the same algorithm that generates the desires in BDIManager should be re-used here:
                    // Where to place it? --> before de-queueing next desire
                    // What's the reason for this? --> after X successful actions executed during executePlan
                    // there might be some desires now that have less priority than before
                    // and the agent will commit to them first
                    // How? --> Break plan execution, re-enqueue un-achieved desire and continue
                    // TODO: currently, if the agent is stuck, it might go into a deadlock where two ClearPathDesires are alternated
                    // re-prioritizing might be a solution
                    // TODO: agentThread is getting too complex, move some logic related to desires to BDIManager
                    // this.desires = bdiManager.calculatePriorities(...);
                    // TODO: add more logging

                    // If some previously solved goals are now unsolved (because the box has been cleared), re-enqueue them!
                    checkAndEnqueueUnsolvedGoalDesires();

                    // Get next desire
                    FibonacciHeap.Entry<Desire> entry;
                    Desire desire;
                    do {
                        // If we just achieved a ClearPathDesire successfully, discard the previous ones that have been enqueued
                        // that wish to clear the same box
                        entry = this.desires.dequeueMin();
                        desire = entry.getValue();
                    }
                    while (this.previouslyAchievedDesire instanceof ClearPathDesire && desire instanceof ClearPathDesire && desire.getBox() == this.previouslyAchievedDesire.getBox());
                    this.previouslyAchievedDesire = null;
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
                        if (desire instanceof GoalDesire) {
                            this.achievedGoalDesiresPriorityMap.put((GoalDesire) desire, entry.getPriority());
                            if (this.previouslyAchievedDesire instanceof ClearPathDesire)
                                this.lockDetector.resetClearingDistance(this.previouslyAchievedDesire.getBox());
                        }
                        //else if (desire instanceof ClearPathDesire)
                        //   this.lockDetector.resetClearingDistance(desire.getBox());
                        this.previouslyAchievedDesire = desire;
                    } catch (InvalidActionException e) {
                        ConsoleLogger.logInfo(LOGGER, e.getMessage());
                        // Current desire wasn't achieved --> add it back to the heap!
                        this.desires.enqueue(entry.getValue(), entry.getPriority());
                        detectBlockingObject(e.getFailedAction(), entry.getValue());
                    } catch (PlanNotFoundException e) {
                        this.lockDetector.planFailed();
                        if (this.lockDetector.needsReplanning()) {
                            // Current desire wasn't achieved --> add it back to the heap!
                            // First failed attempt allowed, will switch to new relaxation
                            ConsoleLogger.logInfo(LOGGER, e.getMessage());
                            this.desires.enqueue(entry.getValue(), entry.getPriority());
                        } else {
                            // Planning keeps failing, unexpected exception. Throw and quit.
                            ConsoleLogger.logError(LOGGER, e.getMessage());
                            System.exit(1);
                            // TODO: there's a bug: with OnlyWallsRelaxation, it's impossible to fail planning:
                            // if code reaches this point it means a ClearPath task has a fake empty cell as target
                            // We need to get rid of them!
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

    private void checkAndEnqueueUnsolvedGoalDesires() {
        Iterator<GoalDesire> it = this.achievedGoalDesiresPriorityMap.keySet().iterator();
        while (it.hasNext()) {
            GoalDesire desire = it.next();
            if (!levelManager.getLevel().isGoalDesireAchieved(desire)) {
                // Avoid picking the same goal if its priority is the lowest!
                // Penalize the desire (+100)
                this.desires.enqueue(desire, this.achievedGoalDesiresPriorityMap.get(desire) + 100);
                it.remove();
            }
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

    /**
     * This method is in charge of letting the agent figure out whether he can solve the blocking issue on its own or
     * he needs to ask for help
     *
     * @param failedAction last action that failed
     * @param desire       current desire to achieve
     */
    private void detectBlockingObject(PrimitiveTask failedAction, Desire desire) {
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
            Box blockingBox = (Box) blockingObject;
            if (blockingBox.getColor() == this.agent.getColor()) {
                // Blocking box is of the same color
                int clearingDistance = this.lockDetector.getClearingDistance(blockingBox);
                List<Coordinate> potentialNewPositions = Coordinate.getEmptyCellsWithFixedDistanceFrom(blockingBox.getCoordinate(), clearingDistance);
                // TODO: there should be a more well-structured heuristic --> the most preferred cells are the edge ones
                // surrounded only by walls/boxes and distant from the goals (e.g. SAtowersOfSaigon5)
                potentialNewPositions.add(this.agent.getCoordinate());
                Map<Object, Integer> distances = new HashMap<>();
                potentialNewPositions.forEach(p -> distances.put(p, Coordinate.manhattanDistance(p, desire.getTarget())));
                Coordinate chosenTarget = (Coordinate) HashMapHelper.getKeyByMaxIntValue(distances);

                // Avoid priority conflicts: increase all priorities by 1
                List<FibonacciHeap.Entry<Desire>> tempDesires = new ArrayList<>();
                while (!this.desires.isEmpty())
                    tempDesires.add(this.desires.dequeueMin());
                tempDesires.forEach(d -> this.desires.enqueue(d.getValue(), d.getPriority() + 1));

                // Create new desire and enqueue it with maximum priority
                ClearPathDesire clearPathDesire = new ClearPathDesire(blockingBox, chosenTarget);
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
