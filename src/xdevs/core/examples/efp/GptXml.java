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
 *  - José Luis Risco Martín
 */
package xdevs.core.examples.efp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author jlrisco
 */
public class GptXml extends Coupled {

    public GptXml(Element xmlCoupled) {
        super(xmlCoupled);
    }

    public static void main(String args[]) {
        Element xmlCoupled = null;
        try {
            DevsLogger.setup(Level.INFO);
            File file = new File("test" + File.separator + "gpt.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document docApplication = builder.parse(file.toURI().toString());
            xmlCoupled = (Element) docApplication.getElementsByTagName("coupled").item(0);

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GptXml.class.getName()).log(Level.SEVERE, null, ex);
        }
        GptXml gpt = new GptXml(xmlCoupled);
        Coordinator coordinator = new Coordinator(gpt);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
    }
}
