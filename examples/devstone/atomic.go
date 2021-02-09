package devstone

import (
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
)

type AtomicDEVStone interface {
	modeling.Atomic
	GetIntCount() uint64
	GetExtCount() uint64
	getInPort() modeling.Port
	getOutPort() modeling.Port
}

type atomicDEVStone struct {
	modeling.Atomic
	useOut                       bool
	iIn, oOut                    modeling.Port
	intDelay, extDelay, prepTime float64
	intCount, extCount           uint64
}

func NewAtomicDEVStone(name string, intDelay float64, extDelay float64, prepTime float64, useOut bool) AtomicDEVStone {
	a := atomicDEVStone{modeling.NewAtomic(name), useOut,
		modeling.NewPort("iIn", make([]int, 0)), modeling.NewPort("oOut", make([]int, 0)),
		intDelay, extDelay, prepTime, 0, 0}
	a.AddInPort(a.iIn)
	a.AddOutPort(a.oOut)
	if intDelay < 0 || extDelay < 0 || prepTime < 0 {
		panic(fmt.Sprintf("Delays (%v,%v,%v) must be greater than or equal to 0", intDelay, extDelay, prepTime))
	}
	return &a
}

func (a *atomicDEVStone) Initialize() {
	a.Passivate()
}

func (a *atomicDEVStone) Exit() {}

func (a *atomicDEVStone) DeltInt() {
	a.intCount++
	a.Passivate()
}

func (a *atomicDEVStone) DeltExt(e float64) {
	a.extCount++
	a.HoldIn(util.ACTIVE, a.prepTime)
}

func (a *atomicDEVStone) DeltCon(e float64) {
	a.DeltInt()
	a.DeltExt(0)
}

func (a *atomicDEVStone) Lambda() {
	if a.useOut {
		a.oOut.AddValue(0)
	}
}

func (a *atomicDEVStone) GetIntCount() uint64 {
	return a.intCount
}

func (a *atomicDEVStone) GetExtCount() uint64 {
	return a.intCount
}

func (a *atomicDEVStone) getInPort() modeling.Port {
	return a.iIn
}

func (a *atomicDEVStone) getOutPort() modeling.Port {
	return a.oOut
}
