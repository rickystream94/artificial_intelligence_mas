package architecture.conflicts;

import java.util.concurrent.ArrayBlockingQueue;

public class ConflictResponseGatherer {

    private ArrayBlockingQueue<ConflictResponse> conflictResponses;

    public ConflictResponseGatherer() {
        this.conflictResponses = new ArrayBlockingQueue<>(1);
    }

    public void addConflictResponse(ConflictResponse response) {
        try {
            this.conflictResponses.put(response);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public ConflictResponse waitForConflictResponse() {
        try {
            return this.conflictResponses.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null; // Unreachable
    }


}
