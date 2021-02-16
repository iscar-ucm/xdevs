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
	"github.com/dacya/xdevs/pkg/modeling"
	"github.com/dacya/xdevs/pkg/util"
)

type Processor struct {
	modeling.Atomic
	iIn            modeling.Port
	oOut           modeling.Port
	currentJob     Job
	processingTime float64
}

func NewProcessor(name string, processingTime float64) *Processor {
	p := Processor{
		modeling.NewAtomic(name),
		modeling.NewPort("iIn", make([]Job, 0)),
		modeling.NewPort("oOut", make([]Job, 0)),
		Job{},
		processingTime,
	}
	p.AddInPort(p.iIn)
	p.AddOutPort(p.oOut)
	return &p
}

func (p *Processor) Initialize() {
	p.Passivate()
}

func (p *Processor) Exit() {}

func (p *Processor) DeltInt() {
	p.Passivate()
}

func (p *Processor) DeltExt(e float64) {
	if p.GetPhase() == util.PASSIVE {
		p.currentJob = p.iIn.GetSingleValue().(Job)
		p.HoldIn(util.ACTIVE, p.processingTime)
	} else {
		p.HoldIn(util.ACTIVE, p.GetSigma()-e)
	}
}

func (p *Processor) DeltCon(e float64) {
	p.DeltInt()
	p.DeltExt(0)
}

func (p *Processor) Lambda() {
	p.oOut.AddValue(p.currentJob)
}
