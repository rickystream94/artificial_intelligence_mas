package architecture;

import architecture.bdi.Desire;
import board.Agent;
import logging.ConsoleLogger;
import planning.HTNPlanner;
import planning.HTNWorldState;
import planning.PrimitivePlan;
import planning.actions.CompoundTask;
import planning.actions.PrimitiveTask;
import planning.actions.SolveGoalTask;
import utils.FibonacciHeap;

import java.util.Queue;
import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());
    private Agent agent;
    private FibonacciHeap<Desire> desires;

    public AgentThread(Agent agent, FibonacciHeap<Desire> desires) {
        this.agent = agent;
        this.desires = desires;
    }

    @Override
    public void run() {
        while (true) {
            // TODO: does stuff (thinks, creates plan, communicates with ActionSenderThread...)
            // 1) Get the percepts --> from the level create a new HTNWorldState
            // 2) Create intention as SolveGoalTask and pass it to the planner
            // 3) Get output of planner --> PrimitivePlan and send it to ActionSenderThread
            try {
                while(!this.desires.isEmpty()) {
                    Desire desire = this.desires.dequeueMin().getValue();
                    HTNWorldState worldState = new HTNWorldState(this.agent,desire.getBox(),desire.getGoal());
                    CompoundTask intention = new SolveGoalTask();
                    HTNPlanner planner = new HTNPlanner(worldState,intention);
                    PrimitivePlan plan = planner.findPlan();
                    Queue<PrimitiveTask> tasks = plan.getTasks();

                    while(!tasks.isEmpty()){
                        ActionSenderThread actionSenderThread = ActionSenderThread.getInstance();
                        actionSenderThread.addPrimitiveAction(tasks.remove(),this.agent);
                    }
                }
                //Thread.sleep(3000);
                //ConsoleLogger.logInfo(LOGGER, "Hi from agent thread number " + Thread.currentThread().getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
