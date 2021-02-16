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
	"github.com/pointlesssoft/godevs/pkg/modeling"
)

type EFP struct {
	modeling.Coupled
	iStart modeling.Port
}

func NewEFP(name string, generatorPeriod float64, processorPeriod float64, transducerPeriod float64) *EFP {
	efp := EFP{
		modeling.NewCoupled(name),
		modeling.NewPort("iStart", make([]Job, 0)),
	}
	efp.AddInPort(efp.iStart)

	ef := NewEF("ef", generatorPeriod, transducerPeriod)
	efp.AddComponent(ef)
	processor := NewProcessor("processor", processorPeriod)
	efp.AddComponent(processor)

	efp.AddCoupling(ef.oOut, processor.iIn)
	efp.AddCoupling(processor.oOut, ef.iIn)
	efp.AddCoupling(efp.iStart, ef.iStart)

	return &efp
}
