package modeling

import (
	"fmt"
	"github.com/pointlesssoft/godevs/util"
)

type Atomic interface {
	Component
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
	GetPhase() string
	SetPhase(phase string)
	GetSigma() float64
	SetSigma(sigma float64)
	ShowState() string
}

func NewAtomic(name string) Atomic {
	c := atomic{NewComponent(name), util.PhasePassive, util.INFINITY}
	return &c
}

type atomic struct {
	Component
	Phase string
	Sigma float64
}

func (a *atomic) TA() float64 {
	return a.Sigma
}

func (a *atomic) DeltInt() {
	panic("implement me")
}

func (a *atomic) DeltExt(e float64) {
	panic("implement me")
}

func (a *atomic) DeltCon(e float64) {
	a.DeltInt()
	a.DeltExt(e)
}

func (a *atomic) Lambda() {
	panic("implement me")
}

func (a *atomic) HoldIn(phase string, sigma float64) {
	a.Phase = phase
	a.Sigma = sigma
}

func (a *atomic) Activate() {
	a.Phase = util.PhaseActive
	a.Sigma = 0
}

func (a *atomic) Passivate() {
	a.Phase = util.PhasePassive
	a.Sigma = util.INFINITY
}

func (a *atomic) PassivateIn(phase string) {
	a.Phase = phase
	a.Sigma = util.INFINITY
}

func (a *atomic) PhaseIs(phase string) bool {
	return a.Phase == phase
}

func (a *atomic) GetPhase() string {
	return a.Phase
}

func (a *atomic) SetPhase(phase string) {
	a.Phase = phase
}

func (a *atomic) GetSigma() float64 {
	return a.Sigma
}

func (a *atomic) SetSigma(sigma float64) {
	a.Sigma = sigma
}

func (a *atomic) ShowState() string {
	state := a.GetName() + " [ "
	state += "\tstate: " + a.Phase
	state += "\tsigma: " + fmt.Sprintf("%f", a.Sigma)
	state += " ]"
	return state
}
