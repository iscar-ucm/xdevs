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

import "github.com/pointlesssoft/godevs/pkg/modeling"

type EF struct {
	modeling.Coupled
	iStart modeling.Port
	iIn    modeling.Port
	oOut   modeling.Port
}

func NewEF(name string, period float64, observationTime float64) *EF {
	ef := EF{
		modeling.NewCoupled(name),
		modeling.NewPort("iStart", make([]Job, 0)),
		modeling.NewPort("iIn", make([]Job, 0)),
		modeling.NewPort("oOut", make([]Job, 0)),
	}
	ef.AddInPort(ef.iStart)
	ef.AddInPort(ef.iIn)
	ef.AddOutPort(ef.oOut)

	generator := NewGenerator("generator", period)
	ef.AddComponent(generator)
	transducer := NewTransducer("transducer", observationTime)
	ef.AddComponent(transducer)

	ef.AddCoupling(ef.iIn, transducer.iSolved)
	ef.AddCoupling(generator.oOut, ef.oOut)
	ef.AddCoupling(generator.oOut, transducer.iArrived)
	ef.AddCoupling(transducer.oOut, generator.iStop)
	ef.AddCoupling(ef.iStart, generator.iStart)

	return &ef
}
