package logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class VerySimpleFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return "[" + record.getLoggerName() + "." +
                record.getSourceMethodName() + "] - " +
                "[" + record.getLevel() + "] - " +
                formatMessage(record) +
                "\n";
    }
}
