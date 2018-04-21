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

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());
    private static int MAGIC_NUMBER = 3;
    private Agent agent;
    private FibonacciHeap<Desire> desires;
    private ActionSenderThread actionSenderThread;
    private BlockingQueue<ResponseEvent> responseEvents;

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
        this.responseEvents = new ArrayBlockingQueue<>(1);
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
                    Desire desire = this.desires.dequeueMin().getValue();

                    // Get percepts
                    HTNWorldState worldState = new HTNWorldState(this.agent, desire.getBox(), desire.getGoal());

                    // Create intention
                    Intention intention = new Intention(new SolveGoalTask());

                    // Plan
                    HTNPlanner planner = new HTNPlanner(worldState, intention);
                    PrimitivePlan plan = planner.findPlan();
                    executePlan(plan);
                }
                //Thread.sleep(3000);
                //ConsoleLogger.logInfo(LOGGER, "Hi from agent thread number " + Thread.currentThread().getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void executePlan(PrimitivePlan plan) {
        Queue<PrimitiveTask> tasks = plan.getTasks();
        int threshold = 0;
        do {
            if(threshold == MAGIC_NUMBER){
                //TODO The agent is stuck, we need to replan? Ask for help?
            }
            this.actionSenderThread.addPrimitiveAction(tasks.peek(), this.agent);
            boolean success = false;
            try {
                ResponseEvent responseEvent = this.responseEvents.take();
                success = responseEvent.getResponseFromServer();
                if(success) {
                    tasks.remove();
                    threshold = 0;
                } else {
                    threshold++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // TODO: based on the value of success, decide what to do next --> keep on sending actions or break? ...
        } while (!tasks.isEmpty());
    }

    public void sendServerResponse(ResponseEvent responseEvent) {
        assert responseEvent.getAgentId() == this.agent.getAgentId();
        this.responseEvents.add(responseEvent);
    }
}
