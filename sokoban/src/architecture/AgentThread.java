package architecture;

import architecture.bdi.Desire;
import architecture.bdi.Intention;
import architecture.fipa.Performative;
import architecture.fipa.PerformativeHelpWithBox;
import architecture.fipa.PerformativeManager;
import board.Agent;
import exceptions.PlanNotFoundException;
import logging.ConsoleLogger;
import planning.HTNPlanner;
import planning.HTNWorldState;
import planning.PrimitivePlan;
import planning.actions.PrimitiveTask;
import planning.actions.SolveGoalTask;
import planning.relaxations.Relaxation;
import planning.relaxations.RelaxationFactory;
import utils.FibonacciHeap;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());
    private static int THRESHOLD = 3;

    private Agent agent;
    private FibonacciHeap<Desire> desires;
    private ActionSenderThread actionSenderThread;
    private BlockingQueue<ResponseEvent> responseEvents;
    private LevelManager levelManager;
    private AgentThreadStatus status;

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
        this.status = AgentThreadStatus.FREE;
    }

    @Override
    public void run() {
        // Preliminary one-time steps
        Relaxation relaxation = RelaxationFactory.getBestRelaxation(this.agent.getColor());

        while (true) { // TODO: or better, while(isLevelSolved())
            /* TODO: ** INTENTIONS AND DESIRES **
        Since the desires can't change (boxes/goals don't disappear from the board), each agent will only have to PRIORITIZE which desire it's currently willing to achieve (each loop iteration? Or at less frequent intervals? ...)
        An INTENTION is something more concrete, which shows how the agent is currently trying to achieve that desire
        (e.g. SolveGoal, SolveConflict, ClearPath, MoveToBox, MoveBoxToGoal --> CompoundTask!)
        Intentions are generated for each agent control loop iteration --> deliberation step */
            try {
                while (!this.desires.isEmpty()) {
                    // Get next desire
                    // TODO: a further check when prioritizing should be performed: there are desires that can't be achieved (box is stuck)
                    // We must check for goals that should be achieved in a specific order --> we need a new CompoundTask type like ClearBox
                    // that should move a blocking box somewhere close to the edges to free the way for the targeted box
                    // TODO: desires re-prioritization to be performed and invoked during planning! e.g. if we haven't achieved our goal
                    // after 5/10 primitive actions return the final plan, re-calculate the priorities and re-evaluate the desires:
                    // there might be some desires now that have less priority than before and the agent will commit to them first
                    // TODO: when there are more goals of same type, agent should prioritize the desires that fulfill
                    // the goals that are at the edges (to avoid blocking other boxes) (example from level SAsokobanLevel96)
                    Desire desire = this.desires.dequeueMin().getValue();
                    this.agent.setCurrentTargetBox(desire.getBox());
                    ConsoleLogger.logInfo(LOGGER, "Agent " + this.agent.getAgentId() + " committing to desire " + desire);

                    // Get percepts
                    HTNWorldState worldState = new HTNWorldState(this.agent, desire.getBox(), desire.getGoal(), relaxation);

                    // Create intention
                    Intention intention = new Intention(new SolveGoalTask());

                    // Plan
                    HTNPlanner planner = new HTNPlanner(worldState, intention);
                    PrimitivePlan plan = planner.findPlan();
                    executePlan(plan);
                }

                // Agent has no more desires --> send NoOP actions
                this.actionSenderThread.addPrimitiveAction(new PrimitiveTask(), this.agent);
                getServerResponse();
            } catch (PlanNotFoundException e) {
                ConsoleLogger.logError(LOGGER, e.getMessage());
                // TODO: re-plan
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void executePlan(PrimitivePlan plan) {
        Queue<PrimitiveTask> tasks = plan.getTasks();
        int actionAttempts = 0;
        do {
            if (actionAttempts == THRESHOLD) {
                // The Performative Message can be a Request, Proposal or Inquirie (I dont think we need this)
                // In here the agent has to figure out why he is stuck and determine the how he needs help
                // Create the message and dispatch it on the Bus
                Performative performative = new PerformativeHelpWithBox(null,this);
                PerformativeManager.getDefault().execute(performative);
            }
            status = AgentThreadStatus.BUSY;
            this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
            try {
                if (getServerResponse()) {
                    tasks.remove();
                    actionAttempts = 0;
                } else {
                    actionAttempts++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            status = AgentThreadStatus.FREE;
        } while (!tasks.isEmpty());
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
