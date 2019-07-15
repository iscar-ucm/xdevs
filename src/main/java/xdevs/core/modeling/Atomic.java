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
package xdevs.core.modeling;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xdevs.core.util.Constants;

/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public abstract class Atomic extends Component {

    // DevsAtomic attributes
    protected String phase = Constants.PHASE_PASSIVE;
    protected double sigma = Constants.INFINITY;

    public Atomic(String name) {
        super(name);
    }

    public Atomic() {
        this(Atomic.class.getSimpleName());
    }

    /**
     * Constructor.
     *
     * @param xmlAtomic in the form:
     * <atomic name="name" phase="passive" sigma="INFINITY" class="lib.atomic...">
     * ...
     * </atomic>
     */
    public Atomic(Element xmlAtomic) {
        this(xmlAtomic.getAttribute("name"));
        setPhase(xmlAtomic.getAttribute("phase"));
        String sigmaAsString = xmlAtomic.getAttribute("sigma");
        if (sigmaAsString.equals("INFINITY")) {
            setSigma(Constants.INFINITY);
        } else {
            setSigma(Double.valueOf(sigmaAsString));
        }
        
        NodeList xmlChildList = xmlAtomic.getChildNodes();
        for (int i = 0; i < xmlChildList.getLength(); ++i) {
            Node xmlNode = xmlChildList.item(i);
            Element xmlElement;
            String nodeName = xmlNode.getNodeName();
            switch (nodeName) {
                case "inport":
                    xmlElement = (Element)xmlNode;
                    super.addInPort(new Port(xmlElement.getAttribute("name")));
                    break;
                case "outport":
                    xmlElement = (Element)xmlNode;
                    super.addOutPort(new Port(xmlElement.getAttribute("name")));
                    break;
                default:
                    break;
            }
        }
        
    }

    // DevsAtomic methods
    public double ta() {
        return sigma;
    }

    public abstract void deltint();

    public abstract void deltext(double e);

    public void deltcon(double e) {
        deltint();
        deltext(0);
    }

    public abstract void lambda();

    public void holdIn(String phase, double sigma) {
        this.phase = phase;
        this.sigma = sigma;
    }

    public void activate() {
        this.phase = Constants.PHASE_ACTIVE;
        this.sigma = 0;
    }

    public void passivate() {
        this.phase = Constants.PHASE_PASSIVE;
        this.sigma = Constants.INFINITY;
    }

    public void passivateIn(String phase) {
        this.phase = phase;
        this.sigma = Constants.INFINITY;
    }

    public boolean phaseIs(String phase) {
        return this.phase.equals(phase);
    }

    public String getPhase() {
        return phase;
    }

    public final void setPhase(String phase) {
        this.phase = phase;
    }

    public double getSigma() {
        return sigma;
    }

    public final void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public String showState() {
        StringBuilder sb = new StringBuilder(name + ":[");
        sb.append("\tstate: ").append(phase);
        sb.append("\t, sigma: ").append(sigma);
        sb.append("]");
        return sb.toString();
    }
}
