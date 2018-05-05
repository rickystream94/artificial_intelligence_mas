package exceptions;

import board.Agent;
import board.Box;

public class StuckByForeignBoxException extends Exception {

    private final Box box;
    private final Agent agent;

    public StuckByForeignBoxException(Agent agent, Box box) {
        this.box = box;
        this.agent = agent;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c is hindered by %s. Asking for help!", agent.getAgentId(), box);
    }
}
