package modeling

import (
	"fmt"
	"github.com/pointlesssoft/godevs/util"
)

type AtomicInterface interface {
	ComponentInterface
	TA() float64
	DeltInt()
	DeltExt(e float64)
	DeltCon(e float64)
	Lambda()
	HoldIn(phase string, sigma float64)
	Activate()
	Passivate()
	PassivateIn(phase string)
	PhaseIs(phase string) bool
	GetComponent() *Component
	GetPhase() string
	SetPhase(phase string)
	GetSigma() float64
	SetSigma(sigma float64)
	ShowState() string
}

type Atomic struct {
	Component
	phase string
	sigma float64
}

func NewAtomic(name string) *Atomic {
	a := Atomic{*NewComponent(name), util.PhasePassive, util.INFINITY}
	return &a
}

func (a *Atomic) TA() float64 {
	return a.sigma
}

func (a *Atomic) DeltInt() {
	panic("implement me")
}

func (a *Atomic) DeltExt(e float64) {
	panic("implement me")
}

func (a *Atomic) DeltCon(e float64) {
	a.DeltInt()
	a.DeltExt(0)
}

func (a *Atomic) Lambda() {
	panic("implement me")
}

func (a *Atomic) HoldIn(phase string, sigma float64) {
	a.phase = phase
	a.sigma = sigma
}

func (a *Atomic) Activate() {
	a.phase = util.PhaseActive
	a.sigma = 0
}

func (a *Atomic) Passivate() {
	a.phase = util.PhasePassive
	a.sigma = util.INFINITY
}

func (a *Atomic) PassivateIn(phase string) {
	a.phase = phase
	a.sigma = util.INFINITY
}

func (a *Atomic) PhaseIs(phase string) bool {
	return a.phase == phase
}

func (a *Atomic) GetComponent() *Component {
	return &a.Component
}

func (a *Atomic) GetPhase() string {
	return a.phase
}

func (a *Atomic) SetPhase(phase string) {
	a.phase = phase
}

func (a *Atomic) GetSigma() float64 {
	return a.sigma
}

func (a *Atomic) SetSigma(sigma float64) {
	a.sigma = sigma
}

func (a *Atomic) ShowState() string {
	state := a.GetName() + " [ "
	state += "\tstate: " + a.phase
	state += "\tsigma: " + fmt.Sprintf("%f", a.sigma)
	state += " ]"
	return state
}
