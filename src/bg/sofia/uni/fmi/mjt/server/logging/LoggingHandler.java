package bg.sofia.uni.fmi.mjt.server.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingHandler {
    private Logger logger;

    public LoggingHandler(Logger logger) {
        this.logger = logger;
    }

    public LoggingHandler(Handler handler) {
        logger = Logger.getAnonymousLogger();
        logger.addHandler(handler);
    }

    public void addHandler(Handler handler) {
        logger.addHandler(handler);
    }

    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    public void log(Level level, String msg) {
        logger.log(level, msg);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoggingHandler)) {
            return false;
        }

        LoggingHandler l = (LoggingHandler) o;
        return this.logger.equals(l.logger);
    }

    @Override
    public int hashCode() {
        return this.logger.hashCode();
    }
}
