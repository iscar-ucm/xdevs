/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples.performance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José L. Risco
 */
public class DevStoneProperties {
    
    private static final Logger logger = Logger.getLogger(DevStoneProperties.class.getName());

    public static enum BenchMarkType {

        LI, HI, HO, HOmem, HOmod
    };

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
    public static final String FLATTEN = "Flatten";
    public static final String MAX_NUMBER_OF_EVENTS = "MaxNumberOfEvents";   

    protected Properties properties = new Properties();
    protected HashSet<String> keys = new HashSet<>();

    public DevStoneProperties(String filePath) {
        keys.add(LOGGER_PATH);
        keys.add(BENCHMARK_NAME);
        keys.add(PREPARATION_TIME);
        keys.add(GENERATOR_PERIOD);
        keys.add(GENERATOR_MAX_EVENTS);
        keys.add(DEPTH);
        keys.add(WIDTH);
        keys.add(INT_DELAY_TIME);
        keys.add(EXT_DELAY_TIME);
        keys.add(NUM_TRIALS);
        keys.add(FLATTEN);
        keys.add(MAX_NUMBER_OF_EVENTS);
        if (filePath != null) {
            try {
                properties.load(new BufferedReader(new FileReader(new File(filePath))));
            } catch (IOException ex) {
                Logger.getLogger(DevStoneProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //getLiStandardProperties();
            //getHiStandardProperties();
            //getHoStandardProperties();
            getHoMemStandardProperties();
            //getHoModStandardProperties();
        }
    }

    public DevStoneProperties() {
        this(null);
    }
    
    public void loadFromCommandLine(String[] args) {
        for(int i=0; i<args.length; ++i) {
            String argument = args[i];
            String[] parts = argument.split("=");
            if(!keys.contains(parts[0])) {
                logger.severe("There is no parameter called " + parts[0] + ".");
            }
            else {
                properties.setProperty(parts[0], parts[1]);                
            }
        }
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
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public final void getLiStandardProperties() {
        properties.clear();
        properties.setProperty(LOGGER_PATH, "DevStoneLI.log");
        properties.setProperty(BENCHMARK_NAME, BenchMarkType.LI.toString());
        properties.setProperty(PREPARATION_TIME, "0.0");
        properties.setProperty(GENERATOR_PERIOD, "1");
        properties.setProperty(GENERATOR_MAX_EVENTS, "1:1:2");
        properties.setProperty(WIDTH, "400:1:401");
        properties.setProperty(DEPTH, "400:1:401");
        properties.setProperty(INT_DELAY_TIME, "0.0");
        properties.setProperty(EXT_DELAY_TIME, "0.0");
        properties.setProperty(NUM_TRIALS, "10");
        properties.setProperty(FLATTEN, "false");
        properties.setProperty(MAX_NUMBER_OF_EVENTS, "0");
    }

    public final void getHiStandardProperties() {
        properties.clear();
        properties.setProperty(LOGGER_PATH, "DevStoneHI.log");
        properties.setProperty(BENCHMARK_NAME, "HI");
        properties.setProperty(PREPARATION_TIME, "0.0");
        properties.setProperty(GENERATOR_PERIOD, "1");
        properties.setProperty(GENERATOR_MAX_EVENTS, "100:1:105");
        properties.setProperty(WIDTH, "50:1:55");
        properties.setProperty(DEPTH, "5:1:10");
        properties.setProperty(INT_DELAY_TIME, "0.0");
        properties.setProperty(EXT_DELAY_TIME, "0.0");
        properties.setProperty(NUM_TRIALS, "1");
        properties.setProperty(FLATTEN, "false");
        properties.setProperty(MAX_NUMBER_OF_EVENTS, "0");
    }

    public final void getHoStandardProperties() {
        properties.clear();
        properties.setProperty(LOGGER_PATH, "DevStoneHO.log");
        properties.setProperty(BENCHMARK_NAME, BenchMarkType.HO.toString());
        properties.setProperty(PREPARATION_TIME, "0.0");
        properties.setProperty(GENERATOR_PERIOD, "1");
        properties.setProperty(GENERATOR_MAX_EVENTS, "100:1:105");
        properties.setProperty(WIDTH, "50:1:55");
        properties.setProperty(DEPTH, "5:1:10");
        properties.setProperty(INT_DELAY_TIME, "0.0");
        properties.setProperty(EXT_DELAY_TIME, "0.0");
        properties.setProperty(NUM_TRIALS, "1");
        properties.setProperty(FLATTEN, "false");
        properties.setProperty(MAX_NUMBER_OF_EVENTS, "0");
    }

    public final void getHoMemStandardProperties() {
        properties.clear();
        properties.setProperty(LOGGER_PATH, "DevStoneHOmem.log");
        properties.setProperty(BENCHMARK_NAME, BenchMarkType.HOmem.toString());
        properties.setProperty(PREPARATION_TIME, "0.0");
        properties.setProperty(GENERATOR_PERIOD, "1");
        properties.setProperty(GENERATOR_MAX_EVENTS, "1:1:2");
        properties.setProperty(WIDTH, "2:1:3");
        properties.setProperty(DEPTH, "7000:1:8000");
        properties.setProperty(INT_DELAY_TIME, "0.0");
        properties.setProperty(EXT_DELAY_TIME, "0.0");
        properties.setProperty(NUM_TRIALS, "1");
        properties.setProperty(FLATTEN, "false");
        properties.setProperty(MAX_NUMBER_OF_EVENTS, "0");
    }

    public final void getHoModStandardProperties() {
        properties.clear();
        properties.setProperty(LOGGER_PATH, "DevStoneHOmod.log");
        properties.setProperty(BENCHMARK_NAME, BenchMarkType.HOmod.toString());
        properties.setProperty(PREPARATION_TIME, "0.0");
        properties.setProperty(GENERATOR_PERIOD, "1");
        properties.setProperty(GENERATOR_MAX_EVENTS, "1:1:3");
        properties.setProperty(WIDTH, "2:1:6");
        properties.setProperty(DEPTH, "2:1:6");
        properties.setProperty(INT_DELAY_TIME, "0.0");
        properties.setProperty(EXT_DELAY_TIME, "0.0");
        properties.setProperty(NUM_TRIALS, "1");
        properties.setProperty(FLATTEN, "false");
        properties.setProperty(MAX_NUMBER_OF_EVENTS, "0");
    }

    public static void saveStandardPropertiesFile() throws IOException {
        DevStoneProperties devStoneProp = new DevStoneProperties(null);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("DevStone" + devStoneProp.getProperty(BENCHMARK_NAME)+ ".properties")));
        writer.write("# Logger relative path:\n");
        writer.write(LOGGER_PATH + " = " + devStoneProp.properties.getProperty(LOGGER_PATH) + "\n\n");

        writer.write("# Benchmark name {" + BenchMarkType.LI.toString() + ", " + BenchMarkType.HI.toString() + ", " + BenchMarkType.HO.toString() + ", " + BenchMarkType.HOmem.toString() + ", " + BenchMarkType.HOmod.toString() + "}:\n");
        writer.write(BENCHMARK_NAME + " = " + devStoneProp.properties.getProperty(BENCHMARK_NAME) + "\n\n");

        writer.write("# Preparation time (double, seconds):\n");
        writer.write(PREPARATION_TIME + " = " + devStoneProp.properties.getProperty(PREPARATION_TIME) + "\n\n");

        writer.write("# Generation period (double, seconds):\n");
        writer.write(GENERATOR_PERIOD + " = " + devStoneProp.properties.getProperty(GENERATOR_PERIOD) + "\n\n");

        writer.write("# Number of events to be injected (integer), the format is START:STEP:END, producing a loop between START and END-1, with an increment in the counter equal to STEP:\n");
        writer.write(GENERATOR_MAX_EVENTS + " = " + devStoneProp.properties.getProperty(GENERATOR_MAX_EVENTS) + "\n\n");

        writer.write("# Width (integer), the format is START:STEP:END:\n");
        writer.write(WIDTH + " = " + devStoneProp.properties.getProperty(WIDTH) + "\n\n");

        writer.write("# Depth (integer), the format is START:STEP:END:\n");
        writer.write(DEPTH + " = " + devStoneProp.properties.getProperty(DEPTH) + "\n\n");

        writer.write("# Delay time of the internal transition function (double, in seconds):\n");
        writer.write(INT_DELAY_TIME + " = " + devStoneProp.properties.getProperty(INT_DELAY_TIME) + "\n\n");

        writer.write("# Delay time of the external transition function (double, in seconds):\n");
        writer.write(EXT_DELAY_TIME + " = " + devStoneProp.properties.getProperty(EXT_DELAY_TIME) + "\n\n");

        writer.write("# Number of runs (integer):\n");
        writer.write(NUM_TRIALS + " = " + devStoneProp.properties.getProperty(NUM_TRIALS) + "\n\n");

        writer.write("# Should the model be flattened?:\n");
        writer.write(FLATTEN + " = " + devStoneProp.properties.getProperty(FLATTEN) + "\n\n");

        writer.write("# Maximum number of total events that can be generated by 1 output of the generator. If equal to 0, it will be ignored:\n");
        writer.write(MAX_NUMBER_OF_EVENTS + " = " + devStoneProp.properties.getProperty(MAX_NUMBER_OF_EVENTS) + "\n\n");

        writer.write("# The information in the logger is a set of measures, in the following order (all in the same line):\n");
        writer.write("# Current trial;\n");
        writer.write("# Number of events injected;\n");
        writer.write("# Width;\n");
        writer.write("# Depth;\n");
        writer.write("# Number of internal transition functions;\n");
        writer.write("# Number of external transition functions;\n");
        writer.write("# Number of TOTAL events processed (both injected and generated);\n");
        writer.write("# Wall clock execution time (including swapping time);\n");

        writer.flush();
        writer.close();
    }

    public static void main(String[] args) {
        try {
            saveStandardPropertiesFile();
        } catch (IOException ex) {
            Logger.getLogger(DevStoneProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
