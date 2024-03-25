package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class LoggingHandlerTest {
    private static LoggingHandler loggingHandler;
    private static Handler handler;
    private static Logger logger;

    @BeforeAll
    static void setUp() {
        logger = Mockito.mock(Logger.class);
        loggingHandler = new LoggingHandler(logger);
    }

    @Test
    void testLogsCorrectly() {
        loggingHandler.log(Level.INFO, "Test message");

        verify(logger).log(Level.INFO, "Test message");
    }

    @Test
    void testAddsHandler() {
        loggingHandler.addHandler(new ConsoleHandler());
        verify(logger).addHandler(any());

    }
}
