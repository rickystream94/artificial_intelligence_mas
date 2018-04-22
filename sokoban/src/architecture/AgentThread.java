package architecture;

import architecture.bdi.Desire;
import architecture.bdi.Intention;
import board.Agent;
import logging.ConsoleLogger;
import planning.HTNPlanner;
import planning.HTNWorldState;
import planning.PrimitivePlan;
import planning.actions.PrimitiveTask;
import planning.actions.SolveGoalTask;
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
    // TODO We need a field to set the agent status: busy/available

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
        this.levelManager = ClientManager.getInstance().getLevelManager();
    }

    @Override
    public void run() {
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
                    Desire desire = this.desires.dequeueMin().getValue();
                    this.agent.setCurrentTargetBox(desire.getBox());
                    ConsoleLogger.logInfo(LOGGER, "Agent " + this.agent.getAgentId() + " committing to desire " + desire);

                    // Get percepts
                    HTNWorldState worldState = new HTNWorldState(this.agent, desire.getBox(), desire.getGoal());

                    // Create intention
                    Intention intention = new Intention(new SolveGoalTask());

                    // Plan
                    HTNPlanner planner = new HTNPlanner(worldState, intention);
                    PrimitivePlan plan = planner.findPlan();
                    executePlan(plan);
                }
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
                //TODO The agent is stuck, we need to replan? Ask for help?
            }
            this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
            try {
                ResponseEvent responseEvent = this.responseEvents.take();
                boolean success = responseEvent.isActionSuccessful();
                if (success) {
                    tasks.remove();
                    actionAttempts = 0;
                } else {
                    actionAttempts++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!tasks.isEmpty());
    }

    public void sendServerResponse(ResponseEvent responseEvent) {
        assert responseEvent.getAgentId() == this.agent.getAgentId();
        this.responseEvents.add(responseEvent);
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
