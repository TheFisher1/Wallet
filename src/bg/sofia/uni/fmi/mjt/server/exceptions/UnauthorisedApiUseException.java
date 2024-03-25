package bg.sofia.uni.fmi.mjt.server.exceptions;

public class UnauthorisedApiUseException extends RuntimeException {
    public UnauthorisedApiUseException(String message) {
        super(message);
    }

    public UnauthorisedApiUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
