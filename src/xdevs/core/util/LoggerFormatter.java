/*
 * Copyright (C) 2014-2015 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * http://www.gnu.org/licenses/
 *
 * Contributors:
 *  - José Luis Risco Martín <jlrisco@ucm.es>
 *  - Saurabh Mittal <smittal@duniptech.com>
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
