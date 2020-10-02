package efp

import (
	"fmt"
	"github.com/pointlesssoft/godevs/modeling"
	"github.com/pointlesssoft/godevs/util"
)

type Generator struct {
	modeling.Atomic
	iStart *modeling.Port
	iStop *modeling.Port
	oOut *modeling.Port
	jobCounter int
	period float64
}

func NewGenerator(name string, period float64) *Generator {
	g := Generator{
		*modeling.NewAtomic(name),
		modeling.NewPort("iStart", make([]Job, 0)),
		modeling.NewPort("iStop", make([]Job, 0)),
		modeling.NewPort("oOut", make([]Job, 0)),
		0,
		period,
	}
	g.AddInPort(g.iStart)
	g.AddInPort(g.iStop)
	g.AddOutPort(g.oOut)
	return &g
}

func (g *Generator) Initialize() {
	g.jobCounter = 1
	g.HoldIn(util.PhaseActive, g.period)
}

func (g *Generator) Exit() {}

func (g *Generator) DeltInt() {
	g.jobCounter++
	g.HoldIn(util.PhaseActive, g.period)
}

func (g *Generator) DeltExt(e float64) {
	g.Passivate()
}

func (g *Generator) Lambda() {
	g.oOut.AddValue(Job{fmt.Sprintf("%v", g.jobCounter), 0})
}
