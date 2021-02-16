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

package efp

import (
	"fmt"
	"github.com/dacya/xdevs/pkg/modeling"
	"github.com/dacya/xdevs/pkg/util"
)

type Generator struct {
	modeling.Atomic
	iStart     modeling.Port
	iStop      modeling.Port
	oOut       modeling.Port
	jobCounter int
	period     float64
}

func NewGenerator(name string, period float64) *Generator {
	g := Generator{
		modeling.NewAtomic(name),
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
	g.HoldIn(util.ACTIVE, g.period)
}

func (g *Generator) Exit() {}

func (g *Generator) DeltInt() {
	g.jobCounter++
	g.HoldIn(util.ACTIVE, g.period)
}

func (g *Generator) DeltExt(e float64) {
	g.Passivate()
}

func (g *Generator) Lambda() {
	g.oOut.AddValue(Job{fmt.Sprintf("%v", g.jobCounter), 0})
}
