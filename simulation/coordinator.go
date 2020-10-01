package simulation

import (
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
	for _, coup := range (*c.model).GetEIC() {
		(*coup).PropagateValues()
	}
	for _, coup := range (*c.model).GetEOC() {
		(*coup).PropagateValues()
	}
}

func (c *coordinator) DeltFcn() {  // TODO voy por aqui
	panic("implement me")
}

func (c *coordinator) PropagateInput() {
	panic("implement me")
}

func (c *coordinator) Clear() {
	panic("implement me")
}

func (c *coordinator) SimInject(e float64, port *modeling.Port, values interface{}) {
	panic("implement me")
}

func (c *coordinator) SimulateIterations(numIterations int) {
	panic("implement me")
}

func (c *coordinator) SimulateTime(timeInterval float64) {
	panic("implement me")
}

func (c *coordinator) GetModel() *modeling.Component {
	return (*c.model).GetComponent()
}
