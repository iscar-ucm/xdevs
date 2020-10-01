package simulation

import "github.com/pointlesssoft/godevs/modeling"

type Simulator interface {
	AbstractSimulator
}

func NewSimulator(clock *SimulationClock, model *modeling.Atomic) Simulator {
	s := simulator{NewAbstractSimulator(clock), model}
	return &s
}
type simulator struct {
	AbstractSimulator
	model *modeling.Atomic
}

func (s *simulator) Initialize() {
	(*s.model).Initialize()
	s.SetTL((*s.GetClock()).GetTime())
	s.SetTN(s.GetTL() + (*(s.model)).TA())
}

func (s *simulator) Exit() {
	(*s.model).Exit()
}

func (s *simulator) TA() float64 {
	return (*s.model).TA()
}

func (s *simulator) DeltFcn() {
	t := (*s.GetClock()).GetTime()
	isInputEmpty := (*s.model).IsInputEmpty()
	if !isInputEmpty || t == s.GetTN() {
		// CASE 1: atomic model timed out and no messages were received -> internal delta
		if isInputEmpty {
			(*s.model).DeltInt()
		} else {
			e := t - s.GetTL()
			// CASE 2: both atomic model timed out and messages were received -> confluent delta
			if t == s.GetTN() {
				(*s.model).DeltCon(e)
			// CASE 3: only messages were received -> external delta
			} else {
				(*s.model).DeltExt(e)
			}
		}
		s.SetTL(t)
		s.SetTN(t + (*s.model).TA())
	}
}

func (s *simulator) Lambda() {
	if (*s.GetClock()).GetTime() == s.GetTN() {
		(*s.model).Lambda()
	}
}

func (s *simulator) Clear() {
	for _, port := range (*s.model).GetInPorts() {
		(*port).Clear()
	}
	for _, port := range (*s.model).GetOutPorts() {
		(*port).Clear()
	}
}

func (s *simulator) GetModel() *modeling.Component {
	return (*s.model).GetComponent()
}
