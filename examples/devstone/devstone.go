package devstone

import (
	"errors"
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
)

type Topology string

const (
	LI    Topology = "LI"
	HI             = "HI"
	HO             = "HO"
	HOmod          = "HOmod"
)

func (t Topology) IsValid() error {
	switch t {
	case LI, HI, HO, HOmod:
		return nil
	}
	return errors.New("invalid topology type")
}

type DEVStone struct {
	modeling.Coupled
	topology                     Topology
	Depth, Width                 uint64
	intDelay, extDelay, prepTime float64
	IntCount, ExtCount           uint64
}

func NewDEVStone(name string, topology Topology, depth uint64, width uint64, intDelay float64, extDelay float64, prepTime float64) *DEVStone {
	if topology.IsValid() != nil {
		panic(fmt.Sprintf("DEVStone topology '%v' is invalid", topology))
	}
	if depth < 1 || width < 1 {
		panic(fmt.Sprintf("NewDEVStone: shapes (%v,%v) must be greater than or equal to 1", depth, width))
	}

	d := DEVStone{modeling.NewCoupled(name), topology, depth, width, intDelay, extDelay, prepTime, 0, 0}
	d.AddInPort(modeling.NewPort("iIn", make([]int, 0)))
	d.AddOutPort(modeling.NewPort("oOut", make([]int, 0)))

	if depth == 1 {
		a := NewAtomicDEVStone("atomic_0_0", intDelay, extDelay, prepTime, true)
		d.AddComponent(a)
		d.AddCoupling(d.GetInPort("iIn"), a.iIn)
		d.AddCoupling(a.oOut, d.GetOutPort("oOut"))
	} else {
		c := NewDEVStone(fmt.Sprintf("%v_%v", name, depth-1), topology, depth-1, width, intDelay, extDelay, prepTime)
		d.AddComponent(c)
		d.AddCoupling(d.GetInPort("iIn"), c.GetInPort("iIn"))
		d.AddCoupling(d.GetOutPort("oOut"), c.GetOutPort("oOut"))
		if topology == HO || topology == HOmod {
			d.AddInPort(modeling.NewPort("iIn2", make([]int, 0)))
			if topology == HO {
				d.AddOutPort(modeling.NewPort("oOut2", make([]int, 0)))
				d.AddCoupling(d.GetInPort("iIn2"), c.GetInPort("iIn2"))
			}
		}
		if topology != HOmod {
			var aPrev *AtomicDEVStone = nil
			for i := uint64(0); i < width; i++ {
				a := NewAtomicDEVStone(fmt.Sprintf("atomic_%v_%v", depth-1, i), intDelay, extDelay, prepTime, true)
				d.AddComponent(a)
				d.AddCoupling(d.GetInPort("iIn"), a.iIn)
				if topology != LI {
					if aPrev != nil {
						d.AddCoupling(aPrev.oOut, a.iIn)
					}
					aPrev = a
					if topology == HO {
						d.AddCoupling(a.oOut, d.GetOutPort("oOut"))
					}
				}
			}
		} else {
			panic("HOmod is not implemented yet")
		}
	}
	return &d
}
