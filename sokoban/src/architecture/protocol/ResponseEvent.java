package architecture.protocol;

public class ResponseEvent {

    private boolean responseFromServer;
    private char agentId;

    public ResponseEvent(char agentId, boolean responseFromServer) {
        this.responseFromServer = responseFromServer;
        this.agentId = agentId;
    }

    public boolean isActionSuccessful() {
        return responseFromServer;
    }

    public char getAgentId() {
        return agentId;
    }
}
