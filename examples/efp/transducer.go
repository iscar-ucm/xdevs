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

type Transducer struct {
	modeling.Atomic
	iArrived        modeling.Port
	iSolved         modeling.Port
	oOut            modeling.Port
	jobsArrived     []Job
	jobsSolved      []Job
	observationTime float64
	totalTA         float64
	clock           float64
}

func NewTransducer(name string, observationTime float64) *Transducer {
	t := Transducer{
		modeling.NewAtomic(name),
		modeling.NewPort("iArrived", make([]Job, 0)),
		modeling.NewPort("iSolved", make([]Job, 0)),
		modeling.NewPort("oOut", make([]Job, 0)),
		nil,
		nil,
		observationTime,
		0,
		0,
	}
	t.AddInPort(t.iArrived)
	t.AddInPort(t.iSolved)
	t.AddOutPort(t.oOut)
	return &t
}

func (t *Transducer) Initialize() {
	t.HoldIn(util.ACTIVE, t.observationTime)
}

func (t *Transducer) Exit() {}

func (t *Transducer) DeltInt() {
	t.clock += t.GetSigma()
	throughput, avgTaTime := 0.0, 0.0
	if t.GetPhase() == util.ACTIVE {
		if len(t.jobsSolved) > 0 {
			avgTaTime = t.totalTA / float64(len(t.jobsSolved))
			if t.clock > 0 {
				throughput = float64(len(t.jobsSolved)) / t.clock
			}
		}
		fmt.Printf("End time: %v\n", t.clock)
		fmt.Printf("Jobs arrived: %v\n", len(t.jobsArrived))
		fmt.Printf("Jobs solved: %v\n", len(t.jobsSolved))
		fmt.Printf("Average TA: %v\n", avgTaTime)
		fmt.Printf("Throughput: %v\n", throughput)
		t.HoldIn("done", 0)
	} else {
		t.Passivate()
	}
}

func (t *Transducer) DeltExt(e float64) {
	t.clock += e
	if t.PhaseIs(util.ACTIVE) {
		job := Job{}
		if !t.iArrived.IsEmpty() {
			job = t.iArrived.GetSingleValue().(Job)
			job.Time = t.clock
			fmt.Printf("Start job %v @ t = %v\n", job.Id, t.clock)
			t.jobsArrived = append(t.jobsArrived, job)
		}
		if !t.iSolved.IsEmpty() {
			job = t.iSolved.GetSingleValue().(Job)
			t.totalTA += t.clock - job.Time
			fmt.Printf("Finish job %v @ t = %v\n", job.Id, t.clock)
			job.Time = t.clock
			t.jobsSolved = append(t.jobsSolved, job)
		}
	}
	t.HoldIn(t.GetPhase(), t.GetSigma()-e)
}

func (t *Transducer) DeltCon(e float64) {
	t.DeltInt()
	t.DeltExt(0)
}

func (t *Transducer) Lambda() {
	if t.PhaseIs("done") {
		t.oOut.AddValue(Job{})
	}
}
