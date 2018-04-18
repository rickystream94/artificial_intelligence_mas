package architecture;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private List<AgentThread> listeners;
    private static EventBus instance;

    private EventBus() {
        this.listeners = new ArrayList<>();
    }

    public static EventBus getDefault() {
        if (instance == null)
            instance = new EventBus();
        return instance;
    }

    public void register(AgentThread agentThread) {
        this.listeners.add(agentThread);
    }

    public void dispatch(List<ResponseEvent> responseEvents) {
        responseEvents.forEach(responseEvent -> listeners.get(responseEvent.getAgentId()).sendServerResponse(responseEvent));
    }
}
