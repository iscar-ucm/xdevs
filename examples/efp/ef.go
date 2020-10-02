package efp

import "github.com/pointlesssoft/godevs/modeling"

type EF struct {
	modeling.Coupled
	iStart *modeling.Port
	iIn *modeling.Port
	oOut *modeling.Port
}

func NewEF(name string, period float64, observationTime float64) *EF {
	ef := EF{
		*modeling.NewCoupled(name),
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