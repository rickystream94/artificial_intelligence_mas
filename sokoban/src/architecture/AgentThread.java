package architecture;

import board.Agent;

public class AgentThread implements Runnable {

    private Agent agent;

    public AgentThread(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void run() {
        while (true) {
            // TODO: does stuff (thinks, creates plan, communicates with ActionSenderThread...)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
