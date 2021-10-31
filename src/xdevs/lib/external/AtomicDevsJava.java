/*
 * Copyright (C) 2014-2021 José Luis Risco Martín <jlrisco@ucm.es>
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
package xdevs.lib.external;

import java.util.Collection;
import java.util.Iterator;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author José L. Risco-Martín
 */
public class AtomicDevsJava extends Atomic {

    private final genDevs.modeling.atomic model;

    @SuppressWarnings({"rawtypes"})
    public AtomicDevsJava(genDevs.modeling.atomic model) {
        super(model.getName());
        this.model = model;
        genDevs.modeling.ports devsJavaPorts = model.mh.getInports();
        Iterator<?> itr = devsJavaPorts.iterator();
        while(itr.hasNext()) {
            genDevs.modeling.port devsJavaPort = (genDevs.modeling.port) itr.next();
            super.addInPort(new Port(devsJavaPort.getName()));
        }
        devsJavaPorts = model.mh.getOutports();
        itr = devsJavaPorts.iterator();
        while(itr.hasNext()) {
            genDevs.modeling.port devsJavaPort = (genDevs.modeling.port) itr.next();
            super.addOutPort(new Port(devsJavaPort.getName()));
        }
    }

    @Override
    public void initialize() {
        model.initialize();
    }
    
    @Override
    public void exit() {
    }


    @Override
    public double ta() {
        double sigmaAux = model.ta();
        if (sigmaAux >= genDevs.modeling.DevsInterface.INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        return sigmaAux;
    }

    @Override
    public void deltint() {
        model.deltint();
    }

    @Override
    public void deltext(double e) {
        super.resume(e);
        genDevs.modeling.message msg = buildMessage();
        model.deltext(e, msg);
    }

    public void deltcon(double e) {
        genDevs.modeling.message msg = buildMessage();
        model.deltcon(e, msg);
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public void lambda() {
        genDevs.modeling.message msg = model.out();
        genDevs.modeling.ContentIteratorInterface itr = msg.mIterator();
        while (itr.hasNext()) {
            genDevs.modeling.ContentInterface devsJavaPort = itr.next();
            Port port = super.getOutPort(devsJavaPort.getPortName());
            if (port != null) {
                port.addValue(devsJavaPort.getValue());
            }
        }
    }

    public genDevs.modeling.message buildMessage() {
        genDevs.modeling.message msg = new genDevs.modeling.message();
        Collection<Port<?>> ports = super.getInPorts();
        for (Port<?> port : ports) {
            String portName = port.getName();
            Collection<?> values = port.getValues();
            for (Object value : values) {
                genDevs.modeling.content con = model.makeContent(portName, (GenCol.entity) value);
                msg.add(con);
            }
        }
        return msg;
    }
}
