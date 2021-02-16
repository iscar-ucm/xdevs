/*
 * Copyright (c) 2021, Román Cárdenas Rodríguez.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package simulation

import (
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
)

type Coordinator struct {
	AbstractSimulator                     // It complies the AbstractSimulator interface.
	model             modeling.Coupled    // Coupled Model associated to the Coordinator.
	simulators        []AbstractSimulator // Slice containing all the children simulators of the Coordinator.
	totalIterations   int                 // Number of iterations simulated so far.
}

// NewCoordinator returns a pointer to a Coordinator.
func NewCoordinator(clock Clock, model modeling.Coupled) *Coordinator {
	c := Coordinator{NewAbstractSimulator(clock), model, nil, 0}
	return &c
}

// NewRootCoordinator creates a new simulation Clock and returns a pointer to a Coordinator.
func NewRootCoordinator(initialTime float64, model modeling.Coupled) *Coordinator {
	return NewCoordinator(NewClock(initialTime), model)
}

// buildHierarchy creates all the children components and adds their corresponding simulator to the simulators list.
func (c *Coordinator) buildHierarchy() {
	components := c.model.GetComponents()
	for _, component := range components {
		var simulator AbstractSimulator
		_, ok := interface{}(component).(modeling.Coupled)
		if ok {
			simulator = NewCoordinator(c.GetClock(), component.(modeling.Coupled))
		} else {
			simulator = NewSimulator(c.GetClock(), component.(modeling.Atomic))
		}
		c.simulators = append(c.simulators, simulator)
	}
}

// Initialize builds the coordinator hierarchy and initializes all the children simulators.
func (c *Coordinator) Initialize() {
	c.buildHierarchy()
	for _, sim := range c.simulators {
		sim.Initialize()
	}
	c.SetTL(c.GetClock().GetTime())
	c.SetTN(c.GetTL() + c.TA())
}

// Exit calls to the Exit function of all the children simulators.
func (c *Coordinator) Exit() {
	for _, sim := range c.simulators {
		sim.Exit()
	}
}

// GetSimulators return a slice with all the children simulators.
func (c *Coordinator) GetSimulators() []AbstractSimulator {
	return c.simulators
}

// TA returns the minimum time advance of all the children simulators.
func (c *Coordinator) TA() float64 {
	tn := util.INFINITY
	for _, sim := range c.simulators {
		simTn := sim.GetTN()
		if simTn < tn {
			tn = simTn
		}
	}
	return tn - c.GetClock().GetTime()
}

// Collect triggers the collection function of all children simulators and propagate the output events.
func (c *Coordinator) Collect() {
	for _, sim := range c.simulators { // TODO parallelize
		sim.Collect()
	}
	c.PropagateOutput()
}

// PropagateOutput propagates output events according to the coupling map.
func (c *Coordinator) PropagateOutput() { // TODO parallelize
	for _, coups := range [][]modeling.Coupling{c.model.GetIC(), c.model.GetEOC()} {
		for _, coup := range coups {
			coup.PropagateValues()
		}
	}
}

// Transition propagates input events and calls to the transition function of the children simulators.
func (c *Coordinator) Transition() { // TODO parallelize
	c.PropagateInput()
	for _, sim := range c.simulators {
		sim.Transition()
	}
	c.SetTL(c.GetClock().GetTime())
	c.SetTN(c.GetTL() + c.TA())
}

// PropagateInput propagates input events.
func (c *Coordinator) PropagateInput() { // TODO parallelize
	for _, coup := range c.model.GetEIC() {
		coup.PropagateValues()
	}
}

// Clear calls to the Clear function of children simulators and cleans the ports of the associated coupled model.
func (c *Coordinator) Clear() {
	for _, sim := range c.simulators {
		sim.Clear()
	}
	for _, ports := range [][]modeling.Port{c.model.GetInPorts(), c.model.GetOutPorts()} {
		for _, port := range ports {
			port.Clear()
		}
	}
}

// SimInject injects values to a port after waiting an elapsed time e.
// It panics if the elapsed time is out of bounds.
func (c *Coordinator) SimInject(e float64, port modeling.Port, values interface{}) {
	time := c.GetClock().GetTime() + e
	if time <= c.GetTN() {
		port.AddValues(values)
		c.GetClock().SetTime(time)
		c.Transition()
		c.Clear()
	} else {
		panic(fmt.Sprintf("Time: %v - ERROR input rejected: elapsed time %v is not in bounds.", c.GetTL(), e))
	}
}

// simulateIteration runs a single simulation cycle.
func (c *Coordinator) simulateIteration() {
	c.GetClock().SetTime(c.GetTN())
	c.Collect()
	c.Transition()
	c.Clear()
	c.totalIterations++
}

// SimulateIterations runs numIterations iterations.
func (c *Coordinator) SimulateIterations(numIterations int) {
	for numIterations > 0 && c.GetTN() < util.INFINITY {
		c.simulateIteration()
		numIterations--
	}
}

// SimulateTime runs as many simulation iterations as required until the simulation clock reaches the desired value.
func (c *Coordinator) SimulateTime(timeInterval float64) {
	tF := c.GetClock().GetTime() + timeInterval
	for c.GetClock().GetTime() < util.INFINITY && c.GetTN() < tF {
		c.simulateIteration()
	}
	c.GetClock().SetTime(tF)
}

//GetModel returns the coupled model associated to the Coordinator.
func (c *Coordinator) GetModel() modeling.Component {
	return c.model
}
