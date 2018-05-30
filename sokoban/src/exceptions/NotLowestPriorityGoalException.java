package exceptions;

import board.Agent;
import board.Goal;

public class NotLowestPriorityGoalException extends Exception {

    private Agent agent;
    private Goal goal;

    public NotLowestPriorityGoalException(Agent agent, Goal goal) {
        this.agent = agent;
        this.goal = goal;
    }

    @Override
    public String getMessage() {
        return String.format("Agent %c: there are goals with lower priority that need to be solved before I can solve %s!", this.agent.getAgentId(), this.goal);
    }
}
