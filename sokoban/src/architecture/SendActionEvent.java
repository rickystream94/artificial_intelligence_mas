package architecture;

import board.Agent;
import planning.actions.PrimitiveTask;

public class SendActionEvent {

    private PrimitiveTask action;
    private Agent agent;

    public SendActionEvent(PrimitiveTask action, Agent agent) {
        this.action = action;
        this.agent = agent;
    }


    public PrimitiveTask getAction() {
        return action;
    }

    public Agent getAgent() {
        return agent;
    }
}
