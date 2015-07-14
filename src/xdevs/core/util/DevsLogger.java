/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Luis Risco Martín
 */
public class DevsLogger {

    public static void setup(String filePath, Level level) {
        Logger logger = Logger.getLogger("");
        logger.setLevel(level);
        try {
            FileHandler fileHandler = new FileHandler(filePath, true);
            logger.addHandler(fileHandler);
            fileHandler.setFormatter(LoggerFormatter.formatter);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            handler.setFormatter(LoggerFormatter.formatter);
            handler.setLevel(level);
        }
    }

    public static void setup(Level level) {
        setup("logger.log", level);
    }

    public static void setup() {
        setup("logger.log", Level.INFO);
    }

    /*    public void setLevel(Level level) {
     logger.setLevel(level);
     }*/
}
