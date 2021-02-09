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
	return errors.New("invalid DEVStone topology type")
}

type DEVStone struct {
	modeling.Coupled
	subDEVStone                  *DEVStone
	topology                     Topology
	Depth, Width                 uint64
	intDelay, extDelay, prepTime float64
	IntCount, ExtCount           uint64
}

func NewDEVStone(name string, topology Topology, depth uint64, width uint64, intDelay float64, extDelay float64, prepTime float64) *DEVStone {
	if topology.IsValid() != nil {
		panic(fmt.Sprintf("the DEVStone topology '%v' is invalid", topology))
	}
	if depth < 1 || width < 1 {
		panic(fmt.Sprintf("depth and width must be greater than or equal to 1, but are (%v,%v)", depth, width))
	}

	d := DEVStone{modeling.NewCoupled(fmt.Sprintf("%v_%v", name, depth-1)), nil, topology, depth, width, intDelay, extDelay, prepTime, 0, 0}
	d.AddInPort(modeling.NewPort("iIn", make([]int, 0)))
	d.AddOutPort(modeling.NewPort("oOut", make([]int, 0)))
	if topology == HO || topology == HOmod {
		d.AddInPort(modeling.NewPort("iIn2", make([]int, 0)))
		if topology == HO {
			d.AddOutPort(modeling.NewPort("oOut2", make([]int, 0)))
		}
	}

	if depth == 1 { // innermost coupled model only contains an atomic model
		a := NewAtomicDEVStone("atomic_0_0", intDelay, extDelay, prepTime, true)
		d.AddComponent(a)
		d.AddCoupling(d.GetInPort("iIn"), a.getInPort())
		d.AddCoupling(a.getOutPort(), d.GetOutPort("oOut"))
	} else {
		if err := d.createSubDEVStone(name); err != nil {
			panic(fmt.Sprintf("DEVStone createSubDEVStone: %v", err))
		}

		switch topology {
		case HOmod:
			if err := d.createHOmodDEVStoneAtomics(); err != nil {
				panic(fmt.Sprintf("DEVStone createHOmodDEVStoneAtomics: %v", err))
			}
		default:
			if err := d.createSimpleDEVStoneAtomics(); err != nil {
				panic(fmt.Sprintf("DEVStone createSimpleDEVStoneAtomics: %v", err))
			}
		}
	}
	return &d
}

func (d *DEVStone) createSubDEVStone(name string) error {
	if d.Depth <= 1 {
		return errors.New("DEVStone model depth must be greater than 1 to have a coupled subDEVStone")
	}
	d.subDEVStone = NewDEVStone(name, d.topology, d.Depth-1, d.Width, d.intDelay, d.extDelay, d.prepTime)
	d.AddComponent(d.subDEVStone)
	d.AddCoupling(d.GetInPort("iIn"), d.subDEVStone.GetInPort("iIn"))
	d.AddCoupling(d.GetOutPort("oOut"), d.subDEVStone.GetOutPort("oOut"))
	if d.topology == HO {
		d.AddCoupling(d.GetInPort("iIn2"), d.subDEVStone.GetInPort("iIn2"))
	}
	return nil
}

func (d *DEVStone) createSimpleDEVStoneAtomics() error {
	useOut := false
	switch d.topology {
	case LI:
	case HI, HO:
		useOut = true
	default:
		return errors.New(fmt.Sprintf("the DEVStone model topology '%v' is not considered as simple", d.topology))
	}

	var aPrev AtomicDEVStone = nil // Atomic model created in the previous iteration of the next loop
	for i := uint64(0); i < d.Width; i++ {
		a := NewAtomicDEVStone(fmt.Sprintf("atomic_%v_%v", d.Depth-1, i), d.intDelay, d.extDelay, d.prepTime, useOut)
		d.AddComponent(a)
		d.AddCoupling(d.GetInPort("iIn"), a.getInPort())
		if d.topology != LI { // LI models don't need any extra steps
			if aPrev != nil {
				d.AddCoupling(aPrev.getOutPort(), a.getInPort())
			}
			if d.topology == HO {
				d.AddCoupling(a.getOutPort(), d.GetOutPort("oOut2"))
			}
		}
		aPrev = a
	}
	return nil
}

func (d *DEVStone) createHOmodDEVStoneAtomics() error {
	if d.topology != HOmod {
		return errors.New(fmt.Sprintf("DEVStone model topology '%v' is incompatible with HOmod", d.topology))
	} else if d.Width < 2 {
		return errors.New(fmt.Sprintf("HOmod model width must be greater than or equal to 2, but is %v", d.Width))
	}
	var prevAtomicsRow []AtomicDEVStone = nil // Atomics created in the previous iteration of the next loop
	for i := uint64(0); i < d.Width; i++ {
		initialJ := uint64(0)
		if i > 1 {
			initialJ = i - 1
		}
		atomicsRow := make([]AtomicDEVStone, d.Width-1-initialJ) // Atomics to be created in this iteration
		for j := initialJ; j < d.Width-1; j++ {
			a := NewAtomicDEVStone(fmt.Sprintf("atomic_%v_%v_%v", d.Depth-1, i, j), d.intDelay, d.extDelay, d.prepTime, true)
			d.AddComponent(a)
			atomicsRow[j-initialJ] = a
			if i == 0 { // First row of atomic models
				d.AddCoupling(d.GetInPort("iIn2"), a.getInPort())
				d.AddCoupling(a.getOutPort(), d.subDEVStone.GetInPort("iIn2"))
			} else { // Remaining rows of atomic models
				if j == initialJ { // First atomic of the row is coupled to the coupled input port
					d.AddCoupling(d.GetInPort("iIn2"), a.getInPort())
				}
				if i == 1 { // Second row of atomic models
					for _, prevAtomic := range prevAtomicsRow {
						d.AddCoupling(a.getOutPort(), prevAtomic.getInPort())
					}
				} else if prevAtomicsRow == nil {
					return errors.New(fmt.Sprintf("row %v could not access to previous row, as it is nil", i))
				} else { // Remaining rows
					d.AddCoupling(a.getOutPort(), prevAtomicsRow[j-initialJ+1].getInPort())
				}
			}
		}
		prevAtomicsRow = atomicsRow
	}
	return nil
}
