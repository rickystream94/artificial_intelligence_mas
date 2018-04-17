package exceptions;

public class NoValidRefinementsException extends Exception {

    @Override
    public String getMessage() {
        return "No valid refinements could be found!";
    }
}
