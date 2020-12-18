/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import xdevs.core.modeling.Atomic;

/**
 *
 * @author Almendras
 */
public interface DistributedInterface {
    public Atomic returnModel(String ClassName);
    public String getSimulationPlane();
}
