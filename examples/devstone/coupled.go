/*
 * Copyright (c) 2021, Román Cárdenas Rodríguez.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package devstone

import (
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
	if err := checkShape(topology, depth, width); err != nil {
		return nil, err
	}
	if err := checkTiming(intDelay, extDelay, prepTime); err != nil {
		return nil, err
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
		a, err := newAtomicDEVStone("atomic_0_0", intDelay, extDelay, prepTime, true)
		if err != nil {
			return nil, err
		}
		d.AddComponent(a)
		d.AddCoupling(d.GetInPort("iIn"), a.GetInPort("iIn"))
		d.AddCoupling(a.GetOutPort("oOut"), d.GetOutPort("oOut"))
	} else {
		d.createSubDEVStone(name)
		if topology == HOmod {
			d.createHOmodDEVStoneAtomics()
		} else {
			d.createSimpleDEVStoneAtomics()
		}
	}
	return &d, nil
}

func (c *coupledDEVStone) createSubDEVStone(name string) {
	if c.Depth <= 1 {
		panic("model depth must be greater than 1 to have a coupled SubDEVStone")
	}
	d, err := NewDEVStone(name, c.topology, c.Depth-1, c.Width, c.intDelay, c.extDelay, c.prepTime)
	if err != nil {
		panic(fmt.Sprintf("createSubDEVStone: %v", err))
	}
	c.subCoupledDEVStone = d
	c.AddComponent(c.subCoupledDEVStone)
	c.AddCoupling(c.GetInPort("iIn"), c.subCoupledDEVStone.GetInPort("iIn"))
	c.AddCoupling(c.GetOutPort("oOut"), c.subCoupledDEVStone.GetOutPort("oOut"))
	if c.topology == HO {
		c.AddCoupling(c.GetInPort("iIn2"), c.subCoupledDEVStone.GetInPort("iIn2"))
	}
}

func (c *coupledDEVStone) createSimpleDEVStoneAtomics() {
	useOut := c.topology == HI || c.topology == HO
	var aPrev DEVStone = nil // Atomic model created in the previous iteration of the next loop
	for i := 0; i < c.Width; i++ {
		a, err := newAtomicDEVStone(fmt.Sprintf("atomic_%v_%v", c.Depth-1, i), c.intDelay, c.extDelay, c.prepTime, useOut)
		if err != nil {
			panic(fmt.Sprintf("createSimpleDEVStoneAtomics: %v", err))
		}
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

func (c *coupledDEVStone) createHOmodDEVStoneAtomics() {
	var prevAtomicsRow []DEVStone = nil // Atomics created in the previous iteration of the next loop
	for i := 0; i < c.Width; i++ {
		initialJ := 0
		if i > 1 {
			initialJ = i - 1
		}
		atomicsRow := make([]DEVStone, c.Width-1-initialJ) // Atomics to be created in this iteration
		for j := initialJ; j < c.Width-1; j++ {
			a, err := newAtomicDEVStone(fmt.Sprintf("atomic_%v_%v_%v", c.Depth-1, i, j), c.intDelay, c.extDelay, c.prepTime, true)
			if err != nil {
				panic(fmt.Sprintf("createHOmodDEVStoneAtomics: %v", err))
			}
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
					panic(fmt.Sprintf("row %v could not access to previous row, as it is nil", i))
				} else { // Remaining rows
					c.AddCoupling(a.GetOutPort("oOut"), prevAtomicsRow[j-initialJ+1].GetInPort("iIn"))
				}
			}
		}
		prevAtomicsRow = atomicsRow
	}
}

func (c *coupledDEVStone) GetIntCount() int {
	n := 0
	for _, c := range c.GetComponents() {
		if s, ok := c.(DEVStone); ok {
			n += s.GetIntCount()
		}
	}
	return n
}

func (c *coupledDEVStone) GetExtCount() int {
	n := 0
	for _, c := range c.GetComponents() {
		if s, ok := c.(DEVStone); ok {
			n += s.GetExtCount()
		}
	}
	return n
}

func (c *coupledDEVStone) GetTotalCount() int {
	n := 0
	for _, c := range c.GetComponents() {
		if s, ok := c.(DEVStone); ok {
			n += s.GetTotalCount()
		}
	}
	return n
}
