package devstone

import (
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
)

type atomicDEVStone struct {
	modeling.Atomic
	useOut                       bool
	iIn, oOut                    modeling.Port
	intDelay, extDelay, prepTime float64
	intCount, extCount           int
}

func newAtomicDEVStone(name string, intDelay float64, extDelay float64, prepTime float64, useOut bool) DEVStone {
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

func (a *atomicDEVStone) getInPort() modeling.Port {
	return a.iIn
}

func (a *atomicDEVStone) getOutPort() modeling.Port {
	return a.oOut
}

func (a *atomicDEVStone) GetIntCount() int {
	return a.intCount
}

func (a *atomicDEVStone) GetExtCount() int {
	return a.extCount
}

func (a *atomicDEVStone) GetTotalCount() int {
	return a.intCount + a.extCount
}
