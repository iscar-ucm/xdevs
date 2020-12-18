/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.examples.distributed.gpt;

import java.util.logging.Level;
import java.util.regex.Pattern;
import xdevs.core.simulation.distributed.CoordinatorDistributed;
import xdevs.core.simulation.distributed.SimulatorDistributed;
import xdevs.core.util.DevsLogger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author Almendras
 *
 * Arguments: args[0] = Type of node: "Atomic" or "Simulator" (*) args[1] = Name
 * (*) args[2] = Period (*) args[3] = Observation Time (*) args[4] = XML name
 * (*) args[5] = Simulation Plane. Example:
 * C;IP;Port1;Port2#G;IP;Port1;Port2#P;IP;Port1;Port2#T;IP;Port1;Port2 args[6] =
 * IP (-) args[7] = Primary Port (-) args[8] = Secondary Port (-)
 *
 * (*) = Required for all (-) = Optional for Simulator
 */
public class Node {

    public static void createXmlFile(String importantData, String XMLName) {
        // Get data
        String[] parts = importantData.split(Pattern.quote("#"));
        String[][] simulationPlane = new String[4][4];
        for (int i = 0; i < parts.length; i++) {
            String[] data = parts[i].split(";");
            for (int j = 0; j < data.length; j++) {
                simulationPlane[i][j] = data[j];
            }
        }
        // Write XML file
        try {
            String content
                    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<simulation>\n"
                    + "    <coordinator name=\"CoordinatorDistributed\" class=\"xdevs.core.simulation.distributed.CoordinatorDistributed\" host=\"" + simulationPlane[0][1] + "\" mainPort=\"" + simulationPlane[0][2] + "\" auxPort=\"" + simulationPlane[0][3] + "\"/>\n"
                    + "    <atomic name=\"Generator\" class=\"xdevs.core.test.efp.Generator\" host=\"" + simulationPlane[1][1] + "\" mainPort=\"" + simulationPlane[1][2] + "\" auxPort=\"" + simulationPlane[1][3] + "\">\n"
                    + "        <inport name=\"stop\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "        <outport name=\"out\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "    </atomic>\n"
                    + "    <atomic name=\"Processor\" class=\"xdevs.core.test.efp.Processor\" host=\"" + simulationPlane[2][1] + "\" mainPort=\"" + simulationPlane[2][2] + "\" auxPort=\"" + simulationPlane[2][3] + "\">\n"
                    + "        <inport name=\"in\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "        <outport name=\"out\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "    </atomic>\n"
                    + "    <atomic name=\"Transducer\" class=\"xdevs.core.test.efp.Transducer\" host=\"" + simulationPlane[3][1] + "\" mainPort=\"" + simulationPlane[3][2] + "\" auxPort=\"" + simulationPlane[3][3] + "\">\n"
                    + "        <inport name=\"solved\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "        <inport name=\"arrived\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "        <outport name=\"out\" class=\"xdevs.core.test.efp.Job\"/>\n"
                    + "    </atomic>\n"
                    + "    <connection atomicFrom=\"Processor\" classFrom=\"xdevs.core.test.efp.Processor\" portFrom=\"oOut\" atomicTo=\"Transducer\" classTo=\"xdevs.core.test.efp.Transducer\" portTo=\"iSolved\"/>\n"
                    + "    <connection atomicFrom=\"Generator\" classFrom=\"xdevs.core.test.efp.Generator\" portFrom=\"oOut\" atomicTo=\"Processor\" classTo=\"xdevs.core.test.efp.Processor\" portTo=\"iIn\"/>\n"
                    + "    <connection atomicFrom=\"Generator\" classFrom=\"xdevs.core.test.efp.Generator\" portFrom=\"oOut\" atomicTo=\"Transducer\" classTo=\"xdevs.core.test.efp.Transducer\" portTo=\"iArrived\"/>\n"
                    + "    <connection atomicFrom=\"Transducer\" classFrom=\"xdevs.core.test.efp.Transducer\" portFrom=\"oOut\" atomicTo=\"Generator\" classTo=\"xdevs.core.test.efp.Generator\" portTo=\"iStop\"/>\n"
                    + "</simulation>";
            File file = new File(XMLName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        GptDistributed gpt = new GptDistributed("GPT", 5, 100, File.separator + "tmp" + File.separator + "example.xml");
        CoordinatorDistributed coordinator = new CoordinatorDistributed(gpt);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }
}
