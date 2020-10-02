package simulation

import "github.com/pointlesssoft/godevs/modeling"

type AbstractSimulatorInterface interface {
	Initialize()
	Exit()
	TA() float64
	Lambda()
	DeltFcn()
	Clear()
	GetModel() modeling.ComponentInterface
	GetTL() float64
	SetTL(tl float64)
	GetTN() float64
	SetTN(tn float64)
	GetClock() *Clock
}

type AbstractSimulator struct {
	clock *Clock
	tL float64  // Time of last event
	tN float64  // Time of next event
}

func NewAbstractSimulator(clock *Clock) *AbstractSimulator {
	s := AbstractSimulator{clock, 0, 0}
	return &s
}

func (a *AbstractSimulator) Initialize() {
	panic("implement me")
}

func (a *AbstractSimulator) Exit() {
	panic("implement me")
}

func (a *AbstractSimulator) TA() float64 {
	panic("implement me")
}

func (a *AbstractSimulator) Lambda() {
	panic("implement me")
}

func (a *AbstractSimulator) DeltFcn() {
	panic("implement me")
}

func (a *AbstractSimulator) Clear() {
	panic("implement me")
}

func (a *AbstractSimulator) GetModel() modeling.ComponentInterface {
	panic("implement me")
}

func (a *AbstractSimulator) GetTL() float64 {
	return a.tL
}

func (a *AbstractSimulator) SetTL(tL float64) {
	a.tL = tL
}

func (a *AbstractSimulator) GetTN() float64 {
	return a.tN
}

func (a *AbstractSimulator) SetTN(tN float64) {
	a.tN = tN
}

func (a *AbstractSimulator) GetClock() *Clock {
	return a.clock
}
