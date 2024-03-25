package bg.sofia.uni.fmi.mjt.server.exceptions;

public class NegativeValueException extends Exception {
    public NegativeValueException(String message) {
        super(message);
    }

    public NegativeValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
