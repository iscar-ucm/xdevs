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

package simulation

import "github.com/dacya/xdevs/pkg/modeling"

type Simulator struct {
	AbstractSimulator                 // It complies the AbstractSimulator interface.
	model             modeling.Atomic // Atomic Model associated to the Simulator.
}

// NewSimulator returns a pointer to a new Simulator.
func NewSimulator(clock Clock, model modeling.Atomic) *Simulator {
	s := Simulator{NewAbstractSimulator(clock), model}
	return &s
}

// Initialize initializes the atomic Model and sets last time and next time to their initial values.
func (s *Simulator) Initialize() {
	s.model.Initialize()
	s.SetTL(s.GetClock().GetTime())
	s.SetTN(s.GetTL() + s.model.TA())
}

// Exit calls to the atomic Model Exit function.
func (s *Simulator) Exit() {
	s.model.Exit()
}

// TA returns the Atomic Model time advance.
func (s *Simulator) TA() float64 {
	return s.model.TA()
}

// Transition checks if the Model has to trigger one of its transition functions and call to the corresponding one.
func (s *Simulator) Transition() {
	t := s.GetClock().GetTime()
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

// Clear clear all the ports of the Atomic Model.
func (s *Simulator) Clear() {
	for _, ports := range [][]modeling.Port{s.model.GetInPorts(), s.model.GetOutPorts()} {
		for _, port := range ports {
			port.Clear()
		}
	}
}

// Collect calls to the output function of the Atomic Model if simulation time is the next time.
func (s *Simulator) Collect() {
	if s.GetClock().GetTime() == s.GetTN() {
		s.model.Lambda()
	}
}

// GetModel returns the Atomic Model
func (s *Simulator) GetModel() modeling.Component {
	return s.model
}
