package architecture;

import board.Agent;
import logging.ConsoleLogger;

import java.util.logging.Logger;

public class AgentThread implements Runnable {

    private static final Logger LOGGER = ConsoleLogger.getLogger(AgentThread.class.getSimpleName());
    private Agent agent;

    public AgentThread(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void run() {
        while (true) {
            // TODO: does stuff (thinks, creates plan, communicates with ActionSenderThread...)
            try {
                Thread.sleep(3000);
                ConsoleLogger.logInfo(LOGGER, "Hi from agent thread number " + Thread.currentThread().getId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
