package efp

import (
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
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
