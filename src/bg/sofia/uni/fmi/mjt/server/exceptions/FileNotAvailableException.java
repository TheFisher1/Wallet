package bg.sofia.uni.fmi.mjt.server.exceptions;

public class FileNotAvailableException extends RuntimeException {
    public FileNotAvailableException(String message) {
        super(message);
    }

    public FileNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
