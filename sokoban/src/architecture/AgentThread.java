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
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());
    private Agent agent;
    private FibonacciHeap<Desire> desires;
    private ActionSenderThread actionSenderThread;

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
        this.actionSenderThread = ClientManager.getInstance().getActionSenderThread();
    }

    @Override
    public void run() {
        while (true) {
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
        do {
            this.actionSenderThread.addPrimitiveAction(tasks.remove(), this.agent);
            // TODO: implement publisher & subscriber pattern! All agent threads should wait for a "response event" from
            // ActionSenderThread saying "here's the response!". Until they receive the event, they should not keep pushing actions!
            // e.g. something like waitForResponse();
        } while (!tasks.isEmpty());
    }
}
