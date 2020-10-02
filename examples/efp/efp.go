package efp

import (
	"github.com/pointlesssoft/godevs/modeling"
)

type EFP struct {
	modeling.Coupled
	iStart modeling.Port
}

func NewEFP(name string, generatorPeriod float64, processorPeriod float64, transducerPeriod float64) *EFP {
	efp := EFP {
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
