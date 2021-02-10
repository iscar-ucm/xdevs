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

package modeling

import (
	"fmt"
	"github.com/pointlesssoft/godevs/pkg/util"
)

type Atomic interface {
	Component                           // Atomic interface complies with the Component interface.
	TA() float64                        // Returns time to be elapsed until next internal transition is triggered.
	DeltInt()                           // Internal transition function.
	DeltExt(e float64)                  // External transition function.
	DeltCon(e float64)                  // Confluent transition function.
	Lambda()                            // Output function.
	HoldIn(phase string, sigma float64) // Sets Atomic model's phase and sigma.
	Activate()                          // Sets Atomic model's phase to "active" and sigma to 0.
	Passivate()                         // Sets Atomic model's phase to "passive" and sigma to Infinity.
	PassivateIn(phase string)           // Sets Atomic model's phase and sigma to Infinity.
	PhaseIs(phase string) bool          // Returns true if Atomic model's phase is phase.
	GetPhase() string                   // Returns the current phase of the model.
	SetPhase(phase string)              // Sets Atomic model's phase.
	GetSigma() float64                  // Returns Atomic model's current sigma.
	SetSigma(sigma float64)             // Sets Atomic model's current sigma.
	ShowState() string                  // Returns a string that represents Atomic model's state.
}

// NewComponent returns a pointer to a structure that complies the Atomic interface.
// name: name of the Atomic model.
func NewAtomic(name string) Atomic {
	a := atomic{NewComponent(name), util.PASSIVE, util.INFINITY}
	return &a
}

type atomic struct {
	Component         // It inherits Component.
	phase     string  // atomic model current phase.
	sigma     float64 // atomic model current sigma.
}

// TA is the time advance function. It returns the time remaining until the next state timeout of the atomic model.
func (a *atomic) TA() float64 {
	return a.sigma
}

// DeltInt is the internal transition function. It is triggered when the current state times out.
// The internal transition function must modify the phase and sigma as desired by the modeler.
// It panics if the model did not define this method, as it acts as an abstract method that needs to be overwritten.
func (a *atomic) DeltInt() {
	panic("Atomic models must implement an internal transition function")
}

// DeltExt is the external transition function. It is triggered when the model receives an event via its output Ports.
// e corresponds to the elapsed time from the last state change to the interruption.
// The external transition function must modify the phase and sigma as desired by the modeler.
// It panics if the model did not define this method, as it acts as an abstract method that needs to be overwritten.
func (a *atomic) DeltExt(e float64) {
	panic("Atomic models must implement an external transition function")
}

// DeltCon is the confluent transition function. It is triggered when the model receives an event via its output Ports
// and the elapsed time coincides with the Atomic model's sigma.
// e corresponds to the elapsed time from the last state change to the interruption. It is equal to sigma.
// The confluent transition function must determine which transition function is triggered first.
// It panics if the model did not define this method, as it acts as an abstract method that needs to be overwritten.
func (a *atomic) DeltCon(e float64) {
	panic("Atomic models must implement a confluent transition function")
}

// Lambda is the output function. It is triggered when the model right before calling the internal transition function.
// The output function may inject new events in output Ports.
// It panics if the model did not define this method, as it acts as an abstract method that needs to be overwritten.
func (a *atomic) Lambda() {
	panic("Atomic models must implement an output function")
}

// HoldIn Sets Atomic model's phase and sigma.
func (a *atomic) HoldIn(phase string, sigma float64) {
	a.phase = phase
	a.sigma = sigma
}

// Activate sets Atomic model's phase to "active" and sigma to 0.
func (a *atomic) Activate() {
	a.phase = util.ACTIVE
	a.sigma = 0
}

// Passivate sets Atomic model's phase to "passive" and sigma to Infinity.
func (a *atomic) Passivate() {
	a.phase = util.PASSIVE
	a.sigma = util.INFINITY
}

// PassivateIn sets Atomic model's phase and sigma to Infinity.
func (a *atomic) PassivateIn(phase string) {
	a.phase = phase
	a.sigma = util.INFINITY
}

// PhaseIs returns true if Atomic model's phase is phase.
func (a *atomic) PhaseIs(phase string) bool {
	return a.phase == phase
}

// GetPhase returns the current phase of the model.
func (a *atomic) GetPhase() string {
	return a.phase
}

// SetPhase sets Atomic model's phase.
func (a *atomic) SetPhase(phase string) {
	a.phase = phase
}

// GetSigma returns Atomic model's current sigma.
func (a *atomic) GetSigma() float64 {
	return a.sigma
}

// SetSigma sets Atomic model's current sigma.
func (a *atomic) SetSigma(sigma float64) {
	a.sigma = sigma
}

// ShowState returns a string that represents Atomic model's state.
func (a *atomic) ShowState() string {
	state := a.GetName() + " [ "
	state += "\tstate: " + a.phase
	state += "\tsigma: " + fmt.Sprintf("%f", a.sigma)
	state += " ]"
	return state
}
