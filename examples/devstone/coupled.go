package devstone

import (
	"errors"
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
)

type coupledDEVStone struct {
	modeling.Coupled
	subCoupledDEVStone           DEVStone
	topology                     Topology
	Depth, Width                 int
	intDelay, extDelay, prepTime float64
}

func newCoupledDEVStone(name string, topology Topology, depth int, width int, intDelay float64, extDelay float64, prepTime float64) (DEVStone, error) {
	if topology.IsValid() != nil {
		return nil, fmt.Errorf("the DEVStone topology '%v' is invalid", topology)
	}
	if depth < 1 || width < 1 {
		return nil, fmt.Errorf("depth and width must be greater than or equal to 1, but are (%v,%v)", depth, width)
	}

	d := coupledDEVStone{modeling.NewCoupled(fmt.Sprintf("%v_%v", name, depth-1)), nil, topology, depth, width, intDelay, extDelay, prepTime}
	d.AddInPort(modeling.NewPort("iIn", make([]int, 0)))
	d.AddOutPort(modeling.NewPort("oOut", make([]int, 0)))
	if topology == HO || topology == HOmod {
		d.AddInPort(modeling.NewPort("iIn2", make([]int, 0)))
		if topology == HO {
			d.AddOutPort(modeling.NewPort("oOut2", make([]int, 0)))
		}
	}

	if depth == 1 { // innermost coupled model only contains an atomic model
		if a, err := newAtomicDEVStone("atomic_0_0", intDelay, extDelay, prepTime, true); err != nil {
			return nil, fmt.Errorf("newCoupledDEVStone: %v", err)
		} else {
			d.AddComponent(a)
			d.AddCoupling(d.GetInPort("iIn"), a.GetInPort("iIn"))
			d.AddCoupling(a.GetOutPort("oOut"), d.GetOutPort("oOut"))
		}
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
	return &d, nil
}

func (c *coupledDEVStone) createSubDEVStone(name string) error {
	if c.Depth <= 1 {
		return errors.New("DEVStone model depth must be greater than 1 to have a coupled SubDEVStone")
	}
	d, err := NewDEVStone(name, c.topology, c.Depth-1, c.Width, c.intDelay, c.extDelay, c.prepTime)
	if err != nil {
		return fmt.Errorf("createSubDEVStone: %v", err)
	}
	c.subCoupledDEVStone = d
	c.AddComponent(c.subCoupledDEVStone)
	c.AddCoupling(c.GetInPort("iIn"), c.subCoupledDEVStone.GetInPort("iIn"))
	c.AddCoupling(c.GetOutPort("oOut"), c.subCoupledDEVStone.GetOutPort("oOut"))
	if c.topology == HO {
		c.AddCoupling(c.GetInPort("iIn2"), c.subCoupledDEVStone.GetInPort("iIn2"))
	}
	return nil
}

func (c *coupledDEVStone) createSimpleDEVStoneAtomics() error {
	useOut := false
	switch c.topology {
	case LI:
	case HI, HO:
		useOut = true
	default:
		return fmt.Errorf("createSimpleDEVStoneAtomics: model topology '%v' is not considered as simple", c.topology)
	}

	var aPrev DEVStone = nil // Atomic model created in the previous iteration of the next loop
	for i := 0; i < c.Width; i++ {
		if a, err := newAtomicDEVStone(fmt.Sprintf("atomic_%v_%v", c.Depth-1, i), c.intDelay, c.extDelay, c.prepTime, useOut); err != nil {
			return fmt.Errorf("createSimpleDEVStoneAtomics: %v", err)
		} else {
			c.AddComponent(a)
			c.AddCoupling(c.GetInPort("iIn"), a.GetInPort("iIn"))
			if c.topology != LI { // LI models don't need any extra steps
				if aPrev != nil {
					c.AddCoupling(aPrev.GetOutPort("oOut"), a.GetInPort("iIn"))
				}
				if c.topology == HO {
					c.AddCoupling(a.GetOutPort("oOut"), c.GetOutPort("oOut2"))
				}
			}
			aPrev = a
		}
	}
	return nil
}

func (c *coupledDEVStone) createHOmodDEVStoneAtomics() error {
	if c.topology != HOmod {
		return fmt.Errorf("createHOmodDEVStoneAtomics: topology '%v' is incompatible with HOmod", c.topology)
	} else if c.Width < 2 {
		return fmt.Errorf("createHOmodDEVStoneAtomics: width must be greater than or equal to 2, but is %v", c.Width)
	}
	var prevAtomicsRow []DEVStone = nil // Atomics created in the previous iteration of the next loop
	for i := 0; i < c.Width; i++ {
		initialJ := 0
		if i > 1 {
			initialJ = i - 1
		}
		atomicsRow := make([]DEVStone, c.Width-1-initialJ) // Atomics to be created in this iteration
		for j := initialJ; j < c.Width-1; j++ {
			if a, err := newAtomicDEVStone(fmt.Sprintf("atomic_%v_%v_%v", c.Depth-1, i, j), c.intDelay, c.extDelay, c.prepTime, true); err != nil {
				return fmt.Errorf("createHOmodDEVStoneAtomics: %v", err)
			} else {
				c.AddComponent(a)
				atomicsRow[j-initialJ] = a
				if i == 0 { // First row of atomic models
					c.AddCoupling(c.GetInPort("iIn2"), a.GetInPort("iIn"))
					c.AddCoupling(a.GetOutPort("oOut"), c.subCoupledDEVStone.GetInPort("iIn2"))
				} else { // Remaining rows of atomic models
					if j == initialJ { // First atomic of the row is coupled to the coupled input port
						c.AddCoupling(c.GetInPort("iIn2"), a.GetInPort("iIn"))
					}
					if i == 1 { // Second row of atomic models
						for _, prevAtomic := range prevAtomicsRow {
							c.AddCoupling(a.GetOutPort("oOut"), prevAtomic.GetInPort("iIn"))
						}
					} else if prevAtomicsRow == nil {
						return errors.New(fmt.Sprintf("row %v could not access to previous row, as it is nil", i))
					} else { // Remaining rows
						c.AddCoupling(a.GetOutPort("oOut"), prevAtomicsRow[j-initialJ+1].GetInPort("iIn"))
					}
				}
			}
		}
		prevAtomicsRow = atomicsRow
	}
	return nil
}

func (c *coupledDEVStone) GetIntCount() int {
	n := 0
	for _, c := range c.GetComponents() {
		if s, ok := c.(DEVStone); ok {
			n += s.GetIntCount()
		} else {
			panic(fmt.Sprintf("submodel '%s' does not comply with the DEVStone interface", c.GetName()))
		}
	}
	return n
}

func (c *coupledDEVStone) GetExtCount() int {
	n := 0
	for _, c := range c.GetComponents() {
		if s, ok := c.(DEVStone); ok {
			n += s.GetExtCount()
		} else {
			panic(fmt.Sprintf("submodel '%s' does not comply with the DEVStone interface", c.GetName()))
		}
	}
	return n
}

func (c *coupledDEVStone) GetTotalCount() int {
	n := 0
	for _, c := range c.GetComponents() {
		if s, ok := c.(DEVStone); ok {
			n += s.GetTotalCount()
		} else {
			panic(fmt.Sprintf("submodel '%s' does not comply with the DEVStone interface", c.GetName()))
		}
	}
	return n
}
