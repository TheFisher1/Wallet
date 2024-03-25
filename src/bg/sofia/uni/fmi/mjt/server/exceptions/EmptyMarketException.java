package bg.sofia.uni.fmi.mjt.server.exceptions;

public class EmptyMarketException extends RuntimeException {
    public EmptyMarketException(String message) {
        super(message);
    }

    public EmptyMarketException(String message, Throwable cause) {
        super(message, cause);
    }
}
