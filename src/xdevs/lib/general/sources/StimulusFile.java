/*
 * Copyright (C) 2014-2016 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
package xdevs.lib.general.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.OutPort;

/**
 *
 * @author jlrisco
 */
public class StimulusFile extends Atomic {

    public class Event {

        protected Double time;
        protected String portName;
        protected Number value;
    }

    private static final Logger logger = Logger.getLogger(StimulusFile.class.getName());
    protected HashMap<String, OutPort> myOutPorts = new HashMap<>();
    
    
    protected BufferedReader stimulusFile;
    protected Event currentEvent = null;
    protected Event nextEvent = null;
    protected LinkedList<Event> events = new LinkedList<>();

    public StimulusFile(String name, String stimulusFilePath) {
        super(name);
        try {
            stimulusFile = new BufferedReader(new FileReader(new File(stimulusFilePath)));
            // First we read all the output ports
            String portName = stimulusFile.readLine();
            while (portName != null && !portName.startsWith("#") && !portName.contains(";")) {
                portName = stimulusFile.readLine();
                OutPort<Double> port = new OutPort<>(portName);
                super.addOutPort(port);
                myOutPorts.put(portName, port);
            }
        } catch (IOException ex) {
            stimulusFile = null;
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize() {
        currentEvent = readNextEvent();
        if (currentEvent != null) {
            events.add(currentEvent);
            nextEvent = readNextEvent();
            while(nextEvent!=null && !(nextEvent.time>currentEvent.time)) {
                events.add(nextEvent);
                nextEvent = readNextEvent();
            }
            super.holdIn("active", currentEvent.time);
        } else {
            super.passivate();
        }
    }

    @Override
    public void exit() {
        try {
            stimulusFile.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deltint() {
        events.clear();
        if (nextEvent != null) {
            double elapsedTime = nextEvent.time - currentEvent.time;
            events.add(nextEvent);
            currentEvent = nextEvent;
            nextEvent = readNextEvent();
            while(nextEvent!=null && !(nextEvent.time>currentEvent.time)) {
                events.add(nextEvent);
                nextEvent = readNextEvent();
            }
            super.holdIn("active", elapsedTime);
        } else {
            super.passivate();
        }
    }
    
    @Override
    public void deltext(double e) {        
    }
    
    @Override
    public void lambda() {
        for(Event event : events) {
            myOutPorts.get(event.portName).addValue(event.value);
        }
    }

    private Event readNextEvent() {
        Event event = null;
        // We take the next event
        try {
            String currentLine = stimulusFile.readLine();
            while (currentLine != null) {
                if (!currentLine.startsWith("#")) {
                    event = new Event();
                    String[] parts = currentLine.split(";");
                    String[] timeParts = parts[0].split(":");
                    String[] millisParts = timeParts[2].split(".");
                    event.time = 60.0 * 60 * Integer.parseInt(timeParts[0]);
                    event.time += 60 * Integer.parseInt(timeParts[1]);
                    event.time += Integer.parseInt(millisParts[0]);
                    event.time += Integer.parseInt(millisParts[1]) / 1000.0;
                    event.portName = parts[1].replaceAll(" ", "");
                    event.value = Double.parseDouble(parts[2]);
                    return event;
                }
                currentLine = stimulusFile.readLine();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return event;
    }

}
