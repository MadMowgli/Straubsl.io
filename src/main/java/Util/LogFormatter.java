package Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    @Override
    public String format(LogRecord record) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(dateFormat.format(new Date(record.getMillis())));
        stringBuilder.append("] - [");
        stringBuilder.append(record.getLoggerName());
        stringBuilder.append("] - [");
        stringBuilder.append(record.getLevel());
        stringBuilder.append("] - [");
        stringBuilder.append(record.getSourceClassName()).append(".");
        stringBuilder.append(record.getSourceMethodName());
        stringBuilder.append("] - [");
        stringBuilder.append(formatMessage(record));
        stringBuilder.append("]\n");

        return stringBuilder.toString();
    }
}