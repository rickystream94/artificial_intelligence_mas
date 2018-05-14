package architecture.conflicts;

public class ConflictResponse {

    private boolean isMaster;

    public ConflictResponse(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public boolean isMaster() {
        return isMaster;
    }
}
