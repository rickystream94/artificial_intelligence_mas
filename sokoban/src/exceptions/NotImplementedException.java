package exceptions;

public class NotImplementedException extends Exception {

    @Override
    public String getMessage() {
        return "This feature has not been implemented yet!";

    }
}
