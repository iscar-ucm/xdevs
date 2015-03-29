/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José L. Risco
 */
public class DevStoneProperties {

    public static enum BenchMarkType {LI, HI, HO, HOmem, HOmod};
    
    public static final String ARRAY_SEPATATOR = ":";
    public static final String LOGGER_PATH = "LoggerPath";
    public static final String BENCHMARK_NAME = "BenchmarkName";
    public static final String PREPARATION_TIME = "PreparationTime";
    public static final String GENERATOR_PERIOD = "GeneratorPeriod";
    public static final String GENERATOR_MAX_EVENTS = "GeneratorMaxEvents";
    public static final String DEPTH = "Depth";
    public static final String WIDTH = "Width";
    public static final String INT_DELAY_TIME = "IntDelayTime";
    public static final String EXT_DELAY_TIME = "ExtDelayTime";
    public static final String NUM_TRIALS = "NumTrials";

    protected Properties properties = new Properties();

    public DevStoneProperties(String filePath) {
        if (filePath != null) {
            try {
                properties.load(new BufferedReader(new FileReader(new File(filePath))));
            } catch (IOException ex) {
                Logger.getLogger(DevStoneProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // LI typical values
            /*properties.setProperty(LOGGER_PATH, "DevStoneLI.log");
            properties.setProperty(BENCHMARK_NAME, BenchMarkType.LI.toString());
            properties.setProperty(PREPARATION_TIME, "0.0");
            properties.setProperty(GENERATOR_PERIOD, "1");
            properties.setProperty(GENERATOR_MAX_EVENTS, "6000:1:6001");
            properties.setProperty(WIDTH, "1000:1:1001");
            properties.setProperty(DEPTH, "5:1:6");
            properties.setProperty(INT_DELAY_TIME, "0.0");
            properties.setProperty(EXT_DELAY_TIME, "0.0");
            properties.setProperty(NUM_TRIALS, "1");*/
            // HI typical values
            /*properties.setProperty(LOGGER_PATH, "DevStoneHI.log");
            properties.setProperty(BENCHMARK_NAME, "HI");
            properties.setProperty(PREPARATION_TIME, "0.0");
            properties.setProperty(GENERATOR_PERIOD, "1");
            properties.setProperty(GENERATOR_MAX_EVENTS, "100:1:101");
            properties.setProperty(WIDTH, "100:1:101");
            properties.setProperty(DEPTH, "5:1:6");
            properties.setProperty(INT_DELAY_TIME, "0.0");
            properties.setProperty(EXT_DELAY_TIME, "0.0");
            properties.setProperty(NUM_TRIALS, "1");*/
            // HO typical values
            /*properties.setProperty(LOGGER_PATH, "DevStoneHO.log");
            properties.setProperty(BENCHMARK_NAME, BenchMarkType.HO.toString());
            properties.setProperty(PREPARATION_TIME, "0.0");
            properties.setProperty(GENERATOR_PERIOD, "1");
            properties.setProperty(GENERATOR_MAX_EVENTS, "1:1:2");
            properties.setProperty(WIDTH, "4:1:5");
            properties.setProperty(DEPTH, "3:1:4");
            properties.setProperty(INT_DELAY_TIME, "0.0");
            properties.setProperty(EXT_DELAY_TIME, "0.0");
            properties.setProperty(NUM_TRIALS, "1");*/
            // HOmod typical values
            properties.setProperty(LOGGER_PATH, "DevStoneHOmod.log");
            properties.setProperty(BENCHMARK_NAME, BenchMarkType.HOmod.toString());
            properties.setProperty(PREPARATION_TIME, "0.0");
            properties.setProperty(GENERATOR_PERIOD, "1");
            properties.setProperty(GENERATOR_MAX_EVENTS, "1:1:2");
            properties.setProperty(WIDTH, "4:1:5");
            properties.setProperty(DEPTH, "2:1:8");
            properties.setProperty(INT_DELAY_TIME, "0.0");
            properties.setProperty(EXT_DELAY_TIME, "0.0");
            properties.setProperty(NUM_TRIALS, "1");
        }
    }

    public DevStoneProperties() {
        this(null);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getPropertyAsInteger(String key) {
        return Integer.valueOf(properties.getProperty(key));
    }

    public int[] getPropertyAsArrayOfInteger(String key) {
        String[] partsAsString = properties.getProperty(key).split(ARRAY_SEPATATOR);
        int[] parts = new int[partsAsString.length];
        for (int i = 0; i < partsAsString.length; ++i) {
            parts[i] = Integer.parseInt(partsAsString[i]);
        }
        return parts;
    }

    public double getPropertyAsDouble(String key) {
        return Double.valueOf(properties.getProperty(key));
    }

    public boolean getPropertyAsBoolean(String key) {
        return Boolean.valueOf(properties.getProperty(key));
    }
}
