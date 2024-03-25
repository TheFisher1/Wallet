package bg.sofia.uni.fmi.mjt.server.exceptions;

public class CurrencyRetrievalException extends RuntimeException {
    public CurrencyRetrievalException(String message) {
        super(message);
    }

    public CurrencyRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

}
