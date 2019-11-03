package simulation

import "github.com/pointlesssoft/godevs/modeling"

type Coordinator interface {
	AbstractSimulator
	buildHierarchy()
	GetSimulators() []*AbstractSimulator
	PropagateOutput()
	PropagateInput()
	SimInject(e float64, port *modeling.Port, values interface{})
	SimulateIterations(numIterations int)
	SimulateTime(timeInterval float64))
}


type coordinator struct {
	AbstractSimulator
	model modeling.Coupled
	simulators []*Simulator
	totalIterations int
}
// TODO this
