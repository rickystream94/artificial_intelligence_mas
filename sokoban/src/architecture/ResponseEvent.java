package architecture;

public class ResponseEvent {

    private boolean responseFromServer;
    private char agentId;

    public ResponseEvent(char agentId, boolean responseFromServer) {
        this.responseFromServer = responseFromServer;
        this.agentId = agentId;
    }

    public boolean getResponseFromServer() {
        return responseFromServer;
    }

    public char getAgentId() {
        return agentId;
    }
}
