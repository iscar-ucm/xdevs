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
	a := atomic{NewComponent(name), util.PASSIVE, util.INFINITY}
	return &a
}

type atomic struct {
	Component
	phase string
	sigma float64
}

func (a *atomic) TA() float64 {
	return a.sigma
}

func (a *atomic) DeltInt() {
	panic("implement me")
}

func (a *atomic) DeltExt(e float64) {
	panic("implement me")
}

func (a *atomic) DeltCon(e float64) {
	a.DeltInt()
	a.DeltExt(0)
}

func (a *atomic) Lambda() {
	panic("implement me")
}

func (a *atomic) HoldIn(phase string, sigma float64) {
	a.phase = phase
	a.sigma = sigma
}

func (a *atomic) Activate() {
	a.phase = util.ACTIVE
	a.sigma = 0
}

func (a *atomic) Passivate() {
	a.phase = util.PASSIVE
	a.sigma = util.INFINITY
}

func (a *atomic) PassivateIn(phase string) {
	a.phase = phase
	a.sigma = util.INFINITY
}

func (a *atomic) PhaseIs(phase string) bool {
	return a.phase == phase
}

func (a *atomic) GetPhase() string {
	return a.phase
}

func (a *atomic) SetPhase(phase string) {
	a.phase = phase
}

func (a *atomic) GetSigma() float64 {
	return a.sigma
}

func (a *atomic) SetSigma(sigma float64) {
	a.sigma = sigma
}

func (a *atomic) ShowState() string {
	state := a.GetName() + " [ "
	state += "\tstate: " + a.phase
	state += "\tsigma: " + fmt.Sprintf("%f", a.sigma)
	state += " ]"
	return state
}
