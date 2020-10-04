package efp

import (
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/util"
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
		p.HoldIn(util.ACTIVE, p.GetSigma() - e)
	}
}

func (p *Processor) DeltCon(e float64) {
	p.DeltInt()
	p.DeltExt(0)
}

func (p *Processor) Lambda() {
	p.oOut.AddValue(p.currentJob)
}
