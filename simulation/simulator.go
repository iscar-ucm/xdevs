package simulation

import "github.com/pointlesssoft/godevs/modeling"

type Simulator struct {
	AbstractSimulator
	model modeling.AtomicInterface
}

func NewSimulator(clock *Clock, model modeling.AtomicInterface) *Simulator {
	s := Simulator{*NewAbstractSimulator(clock), model}
	return &s
}

func (s *Simulator) Initialize() {
	s.model.Initialize()
	s.SetTL((*s.GetClock()).GetTime())
	s.SetTN(s.GetTL() + s.model.TA())
}

func (s *Simulator) Exit() {
	s.model.Exit()
}

func (s *Simulator) TA() float64 {
	return s.model.TA()
}

func (s *Simulator) DeltFcn() {
	t := (*s.GetClock()).GetTime()
	isInputEmpty := s.model.IsInputEmpty()
	if !isInputEmpty || t == s.GetTN() {
		// CASE 1: atomic model timed out and no messages were received -> internal delta
		if isInputEmpty {
			s.model.DeltInt()
		} else {
			e := t - s.GetTL()
			// CASE 2: both atomic model timed out and messages were received -> confluent delta
			if t == s.GetTN() {
				s.model.DeltCon(e)
			// CASE 3: only messages were received -> external delta
			} else {
				s.model.DeltExt(e)
			}
		}
		s.SetTL(t)
		s.SetTN(t + s.model.TA())
	}
}

func (s *Simulator) Clear() {
	for _, ports := range [][]*modeling.Port{s.model.GetInPorts(), s.model.GetOutPorts()} {
		for _, port := range ports {
			port.Clear()
		}
	}
}

func (s *Simulator) Lambda() {
	if s.GetClock().GetTime() == s.GetTN() {
		s.model.Lambda()
	}
}

func (s *Simulator) GetModel() modeling.ComponentInterface {
	return s.model.GetComponent()
}
