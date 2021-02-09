package devstone

import (
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
)

type AtomicDEVStone struct {
	modeling.Atomic
	useOut                       bool
	iIn, oOut                    modeling.Port
	intDelay, extDelay, prepTime float64
	IntCount, ExtCount           uint64
}

func NewAtomicDEVStone(name string, intDelay float64, extDelay float64, prepTime float64, useOut bool) *AtomicDEVStone {
	a := AtomicDEVStone{modeling.NewAtomic(name), useOut,
		modeling.NewPort("iIn", make([]int, 0)), modeling.NewPort("oOut", make([]int, 0)),
		intDelay, extDelay, prepTime, 0, 0}
	a.AddInPort(a.iIn)
	a.AddOutPort(a.oOut)
	if intDelay < 0 || extDelay < 0 || prepTime < 0 {
		panic(fmt.Sprintf("Delays (%v,%v,%v) must be greater than or equal to 0", intDelay, extDelay, prepTime))
	}
	return &a
}

func (a *AtomicDEVStone) Initialize() {
	a.Passivate()
}

func (a *AtomicDEVStone) Exit() {}

func (a *AtomicDEVStone) DeltInt() {
	a.IntCount++
	a.Passivate()
}

func (a *AtomicDEVStone) DeltExt(e float64) {
	a.ExtCount++
	a.HoldIn(util.ACTIVE, a.prepTime)
}

func (a *AtomicDEVStone) DeltCon(e float64) {
	a.DeltInt()
	a.DeltExt(0)
}

func (a *AtomicDEVStone) Lambda() {
	if a.useOut {
		a.oOut.AddValue(0)
	}
}
