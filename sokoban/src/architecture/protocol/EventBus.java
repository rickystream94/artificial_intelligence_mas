package architecture.protocol;

import architecture.agent.AgentThread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {

    private Map<Character, AgentThread> listeners;
    private static EventBus instance;

    private EventBus() {
        this.listeners = new HashMap<>();
    }

    public static EventBus getDefault() {
        if (instance == null)
            instance = new EventBus();
        return instance;
    }

    public void register(AgentThread agentThread) {
        this.listeners.put(agentThread.getAgent().getAgentId(), agentThread);
    }

    public void dispatch(List<ResponseEvent> responseEvents) {
        responseEvents.forEach(responseEvent -> listeners.get(responseEvent.getAgentId()).sendServerResponse(responseEvent));
    }
}
