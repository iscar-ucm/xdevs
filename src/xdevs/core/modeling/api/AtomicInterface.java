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
package xdevs.core.modeling.api;


/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public interface AtomicInterface extends ComponentInterface {
    public double ta();
    public void deltint();
    public void deltext(double e);
    public void deltcon(double e);
    public void lambda();
    public void holdIn(String phase, double sigma);
    public void activate();
    public void passivate();
    public void passivateIn(String phase);
    public boolean phaseIs(String phase);
    public String getPhase();
    public void setPhase(String phase);
    public double getSigma();
    public void setSigma(double sigma);
    public String showState();
}
