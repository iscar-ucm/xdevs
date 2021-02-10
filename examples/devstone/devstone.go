package devstone

import (
	"errors"
	"github.com/pointlesssoft/godevs/pkg/modeling"
)

type Topology string

const (
	LI    Topology = "LI"
	HI             = "HI"
	HO             = "HO"
	HOmod          = "HOmod"
)

func (t Topology) IsValid() error {
	switch t {
	case LI, HI, HO, HOmod:
		return nil
	}
	return errors.New("invalid DEVStone topology type")
}

type DEVStone interface {
	modeling.Component
	GetIntCount() int
	GetExtCount() int
	GetTotalCount() int
}

func NewDEVStone(name string, topology Topology, depth int, width int, intDelay float64, extDelay float64, prepTime float64) DEVStone {
	return newCoupledDEVStone(name, topology, depth, width, intDelay, extDelay, prepTime)
}
