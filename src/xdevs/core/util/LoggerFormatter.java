/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.util;

import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author jlrisco
 */
public class LoggerFormatter extends Formatter {

  private static final MessageFormat messageFormat = new MessageFormat("[{0}-{1}|{2}]: {3} \n");
  public static final LoggerFormatter formatter = new LoggerFormatter();
  protected long startTime;

  public LoggerFormatter() {
    super();
    startTime = System.currentTimeMillis();
  }

  @Override
  public String format(LogRecord record) {
    Object[] arguments = new Object[4];
    arguments[0] = record.getLevel();
    arguments[1] = Thread.currentThread().getName();
    arguments[2] = getElapsedTime(record.getMillis());
    arguments[3] = record.getMessage();
    return messageFormat.format(arguments);
  }

  public String getElapsedTime(long currentTime) {
    long elapsedTime = currentTime - startTime;
    long elapsedTimeInSeconds = elapsedTime / 1000;
    String format1 = String.format("%%0%dd", 2);
    String format2 = String.format("%%0%dd", 3);
    String seconds = String.format(format1, elapsedTimeInSeconds % 60);
    String minutes = String.format(format1, (elapsedTimeInSeconds % 3600) / 60);
    String hours = String.format(format1, elapsedTimeInSeconds / 3600);
    String millis = String.format(format2, elapsedTime % 1000);
    String time = hours + ":" + minutes + ":" + seconds + "." + millis;
    return time;
  }
}
