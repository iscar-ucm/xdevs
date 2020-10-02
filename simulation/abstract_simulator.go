package simulation

import "github.com/pointlesssoft/godevs/modeling"

type AbstractSimulator interface {
	Initialize()
	Exit()
	TA() float64
	Lambda()
	DeltFcn()
	Clear()
	GetModel() modeling.Component
	GetTL() float64
	SetTL(tl float64)
	GetTN() float64
	SetTN(tn float64)
	GetClock() Clock
}

func NewAbstractSimulator(clock Clock) AbstractSimulator {
	s := abstractSimulator{clock, 0, 0}
	return &s
}

type abstractSimulator struct {
	clock Clock
	tL float64  // Time of last event
	tN float64  // Time of next event
}

func (a *abstractSimulator) Initialize() {
	panic("implement me")
}

func (a *abstractSimulator) Exit() {
	panic("implement me")
}

func (a *abstractSimulator) TA() float64 {
	panic("implement me")
}

func (a *abstractSimulator) Lambda() {
	panic("implement me")
}

func (a *abstractSimulator) DeltFcn() {
	panic("implement me")
}

func (a *abstractSimulator) Clear() {
	panic("implement me")
}

func (a *abstractSimulator) GetModel() modeling.Component {
	panic("implement me")
}

func (a *abstractSimulator) GetTL() float64 {
	return a.tL
}

func (a *abstractSimulator) SetTL(tL float64) {
	a.tL = tL
}

func (a *abstractSimulator) GetTN() float64 {
	return a.tN
}

func (a *abstractSimulator) SetTN(tN float64) {
	a.tN = tN
}

func (a *abstractSimulator) GetClock() Clock {
	return a.clock
}
