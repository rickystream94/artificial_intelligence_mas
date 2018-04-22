package exceptions;

public class PlanNotFoundException extends Exception {

    @Override
    public String getMessage() {
        return "No plan could be found to achieve the current desire.";
    }
}
