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
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
)

type atomicDEVStone struct {
	modeling.Atomic
	useOut                       bool
	iIn, oOut                    modeling.Port
	intDelay, extDelay, prepTime float64
	eventCount                   int
}

func newAtomicDEVStone(name string, intDelay float64, extDelay float64, prepTime float64, useOut bool) (DEVStone, error) {
	if err := checkTiming(intDelay, extDelay, prepTime); err != nil {
		return nil, err
	}
	a := atomicDEVStone{modeling.NewAtomic(name), useOut,
		modeling.NewPort("iIn", make([]int, 0)), modeling.NewPort("oOut", make([]int, 0)),
		intDelay, extDelay, prepTime, 0}
	a.AddInPort(a.iIn)
	a.AddOutPort(a.oOut)
	return &a, nil
}

func (a *atomicDEVStone) Initialize() {
	a.Passivate()
}

func (a *atomicDEVStone) Exit() {}

func (a *atomicDEVStone) DeltInt() {
	a.Passivate()
}

func (a *atomicDEVStone) DeltExt(e float64) {
	a.eventCount++
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

func (a *atomicDEVStone) GetEventCount() int {
	return a.eventCount
}
