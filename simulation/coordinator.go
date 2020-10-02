package simulation

import (
	"fmt"
	"github.com/pointlesssoft/godevs/modeling"
	"github.com/pointlesssoft/godevs/util"
)

type Coordinator struct {
	AbstractSimulator
	model modeling.CoupledInterface
	simulators []AbstractSimulatorInterface
	totalIterations int
}

func NewRootCoordinator(initialTime float64, model modeling.CoupledInterface) *Coordinator {
	return NewCoordinator(NewSimulationClock(initialTime), model)
}

func NewCoordinator(clock *Clock, model modeling.CoupledInterface) *Coordinator {
	c := Coordinator{*NewAbstractSimulator(clock), model, nil, 0}
	return &c
}

func (c *Coordinator) buildHierarchy() {
	components := c.model.GetComponents()
	for _, component := range components {
		var simulator AbstractSimulatorInterface
		_, ok := interface{}(component).(modeling.CoupledInterface)
		if ok {
			simulator = NewCoordinator(c.clock, component.(modeling.CoupledInterface))
		} else {
			simulator = NewSimulator(c.clock, component.(modeling.AtomicInterface))
		}
		c.simulators = append(c.simulators, simulator)
	}
}

func (c *Coordinator) Initialize() {
	c.buildHierarchy()
	for _, sim := range c.simulators {
		sim.Initialize()
	}
	c.SetTL(c.GetClock().GetTime())
	c.SetTN(c.GetTL() + c.TA())
}

func (c *Coordinator) Exit() {
	for _, sim := range c.simulators {
		sim.Exit()
	}
}

func (c *Coordinator) GetSimulators() []AbstractSimulatorInterface {
	return c.simulators
}

func (c *Coordinator) TA() float64 {
	tn := util.INFINITY
	for _, sim := range c.simulators {
		simTn := sim.GetTN()
		if  simTn < tn {
			tn = simTn
		}
	}
	return tn - c.GetClock().GetTime()
}

func (c *Coordinator) Lambda() {
	for _, sim := range c.simulators {  // TODO parallelize
		sim.Lambda()
		c.PropagateOutput()
	}
}

func (c *Coordinator) PropagateOutput() {  // TODO parallelize
	for _, coup := range c.model.GetIC() {
		coup.PropagateValues()
	}
	for _, coup := range c.model.GetEOC() {
		coup.PropagateValues()
	}
}

func (c *Coordinator) DeltFcn() {  // TODO parallelize
	c.PropagateInput()
	for _, sim := range c.simulators {
		sim.DeltFcn()
	}
	c.SetTL(c.GetClock().GetTime())
	c.SetTN(c.GetTL() + c.TA())
}

func (c *Coordinator) PropagateInput() {  // TODO parallelize
	for _, coup := range c.model.GetEIC() {
		coup.PropagateValues()
	}
}

func (c *Coordinator) Clear() {
	for _, sim := range c.simulators {
		sim.Clear()
	}
	for _, ports := range [][]*modeling.Port{c.model.GetInPorts(), c.model.GetOutPorts()} {
		for _, port := range ports {
			port.Clear()
		}
	}
}

func (c *Coordinator) SimInject(e float64, port *modeling.Port, values interface{}) {
	time := c.GetClock().GetTime() + e
	if time <= c.GetTN() {
		port.AddValues(values)
		c.GetClock().SetTime(time)
		c.DeltFcn()
		c.Clear()
	} else {
		panic(fmt.Sprintf("Time: %v - ERROR input rejected: elapsed time %v is not in bounds.", c.GetTL(), e))
	}
}

func (c *Coordinator) SimulateIterations(numIterations int) {
	for numIterations > 0 && c.GetTN() < util.INFINITY {
		c.GetClock().SetTime(c.GetTN())
		c.Lambda()
		c.DeltFcn()
		c.Clear()
		c.totalIterations++
		numIterations--
	}
}

func (c *Coordinator) SimulateTime(timeInterval float64) {
	tF := c.GetClock().GetTime() + timeInterval
	for c.GetClock().GetTime() < util.INFINITY && c.GetTN() < tF {
		c.GetClock().SetTime(c.GetTN())
		c.Lambda()
		c.DeltFcn()
		c.Clear()
	}
	c.GetClock().SetTime(tF)
}

func (c *Coordinator) GetModel() modeling.ComponentInterface {
	return c.model.GetComponent()
}
