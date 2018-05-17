package logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class ConsoleLogger {

    public static Logger getLogger(String className) {
        Logger logger = Logger.getLogger(className);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new VerySimpleFormatter());
        logger.addHandler(handler);
        return logger;
    }

    public static void logInfo(Logger logger, String message) {
        /*logger.info(message);*/
    }

    public static void logError(Logger logger, String message) {
        /*logger.severe(message);*/
    }
}
