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
	"reflect"
)

type Coupling interface {
	GetPortFrom() Port // returns source Port.
	GetPortTo() Port   // returns destination Port.
	PropagateValues()  // copies values from source Port to destination Port.
	String() string    // returns a string representation of the Coupling.
}

// NewCoupling returns a pointer to a structure that complies the Coupling interface.
// portFrom: source Port.
// portTo: destination Port.
// It panics if the data type of the Ports is not the same.
func NewCoupling(portFrom Port, portTo Port) Coupling {
	if reflect.TypeOf(portFrom.GetValues()) != reflect.TypeOf(portTo.GetValues()) {
		panic("Both ports of a coupling must be of the same type")
	}
	c := coupling{portFrom, portTo}
	return &c
}

type coupling struct {
	portFrom Port // source Port.
	portTo   Port // destination Port.
}

// GetPortFrom returns source Port of the coupling.
func (c *coupling) GetPortFrom() Port {
	return c.portFrom
}

// GetPortTo returns destination Port of the coupling.
func (c *coupling) GetPortTo() Port {
	return c.portTo
}

// PropagateValues copies every value in source Port to destination Port.
func (c *coupling) PropagateValues() {
	c.portTo.AddValues(c.portFrom.GetValues())
}

// String returns a string representation of the coupling.
func (c *coupling) String() string {
	return fmt.Sprintf("(%v -> %v)", c.portFrom, c.portTo)
}
