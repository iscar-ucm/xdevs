package simulation

import (
	"fmt"
	"github.com/pointlesssoft/godevs/modeling"
	"github.com/pointlesssoft/godevs/util"
)

type Coordinator interface {
	AbstractSimulator
	buildHierarchy()
	GetSimulators() []*AbstractSimulator
	PropagateOutput()
	PropagateInput()
	SimInject(e float64, port *modeling.Port, values interface{})
	SimulateIterations(numIterations int)
	SimulateTime(timeInterval float64)
}

func NewCoordinator(clock *SimulationClock, model *modeling.Coupled) Coordinator {
	c := coordinator{NewAbstractSimulator(clock), model, nil, 0}
	return &c
}

type coordinator struct {
	AbstractSimulator
	model *modeling.Coupled
	simulators []*AbstractSimulator
	totalIterations int
}

func (c *coordinator) buildHierarchy() {
	panic("implement me")
}

func (c *coordinator) Initialize() {
	c.buildHierarchy()
	for _, sim := range c.simulators {
		(*sim).Initialize()
	}
	c.SetTL((*c.GetClock()).GetTime())
	c.SetTN(c.GetTL() + c.TA())
}

func (c *coordinator) Exit() {
	for _, sim := range c.simulators {
		(*sim).Exit()
	}
}

func (c *coordinator) GetSimulators() []*AbstractSimulator {
	return c.simulators
}

func (c *coordinator) TA() float64 {
	tn := util.INFINITY
	for _, sim := range c.simulators {
		simTn := (*sim).GetTN()
		if  simTn < tn {
			tn = simTn
		}
	}
	return tn - (*c.GetClock()).GetTime()
}

func (c *coordinator) Lambda() {
	for _, sim := range c.simulators {  // TODO parallelize
		(*sim).Lambda()
		c.PropagateOutput()
	}
}

func (c *coordinator) PropagateOutput() {  // TODO parallelize
	for _, coup := range (*c.model).GetIC() {
		(*coup).PropagateValues()
	}
	for _, coup := range (*c.model).GetEOC() {
		(*coup).PropagateValues()
	}
}

func (c *coordinator) DeltFcn() {  // TODO parallelize
	c.PropagateInput()
	for _, sim := range c.simulators {
		(*sim).DeltFcn()
	}
	c.SetTL((*c.GetClock()).GetTime())
	c.SetTN(c.GetTL() + c.TA())
}

func (c *coordinator) PropagateInput() {  // TODO parallelize
	for _, coup := range (*c.model).GetEIC() {
		(*coup).PropagateValues()
	}
}

func (c *coordinator) Clear() {
	for _, sim := range c.simulators {
		(*sim).Clear()
	}
	for _, port := range (*c.model).GetInPorts() {
		(*port).Clear()
	}
	for _, port := range (*c.model).GetOutPorts() {
		(*port).Clear()
	}
}

func (c *coordinator) SimInject(e float64, port *modeling.Port, values interface{}) {
	time := (*c.GetClock()).GetTime() + e
	if time <= c.GetTN() {
		(*port).AddValues(values)
		(*c.GetClock()).SetTime(time)
		c.DeltFcn()
		c.Clear()
	} else {
		panic(fmt.Sprintf("Time: %v - ERROR input rejected: elapsed time %v is not in bounds.", c.GetTL(), e))
	}
}

func (c *coordinator) SimulateIterations(numIterations int) {
	for numIterations > 0 && c.GetTN() < util.INFINITY {
		(*c.GetClock()).SetTime(c.GetTN())
		c.Lambda()
		c.DeltFcn()
		c.Clear()
		c.totalIterations++
		numIterations--
	}
}

func (c *coordinator) SimulateTime(timeInterval float64) {
	tF := (*c.GetClock()).GetTime() + timeInterval
	for (*c.GetClock()).GetTime() < util.INFINITY && c.GetTN() < tF {
		(*c.GetClock()).SetTime(c.GetTN())
		c.Lambda()
		c.DeltFcn()
		c.Clear()
	}
	(*c.GetClock()).SetTime(tF)
}

func (c *coordinator) GetModel() *modeling.Component {
	return (*c.model).GetComponent()
}
