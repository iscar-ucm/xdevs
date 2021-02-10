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

type Component interface {
	GetName() string                 // returns Component's name.
	Initialize()                     // initializes the component before simulation.
	Exit()                           // performs a set of operations after simulation.
	IsInputEmpty() bool              // returns true if none of the input Ports contains any value.
	AddInPort(port Port)             // adds a new input Port.
	GetInPort(portName string) Port  // returns one input Port with an specific name.
	GetInPorts() []Port              // returns a slice with all the input Ports.
	AddOutPort(port Port)            // adds a new output Port.
	GetOutPort(portName string) Port // returns one output Port with an specific name.
	GetOutPorts() []Port             // returns a slice with all the output Ports.
	setParent(component Component)   // sets one Component as parent of the Component.
	GetParent() Component            // returns parent Component.
	String() string                  // returns a string representation of the Component.
}

// NewComponent returns a pointer to a structure that complies the Component interface.
// name: name of the port.
func NewComponent(name string) Component {
	c := component{name, nil, nil, nil}
	return &c
}

type component struct {
	name     string    // component's name.
	parent   Component // parent Component of the component.
	inPorts  []Port    // set of input ports. TODO map?
	outPorts []Port    // set of output ports. TODO map?
}

// getPort returns a Port from a slice of Ports if it has a given name.
// It returns an error if no Port with the desired name is part of Ports slice.
func (c *component) getPort(ports []Port, portName string) Port {
	for _, p := range ports {
		if p.GetName() == portName {
			return p
		}
	}
	return nil
}

// GetName returns the component's name.
func (c *component) GetName() string {
	return c.name
}

// Initialize performs all the required operations before starting a simulation.
func (c *component) Initialize() {
	panic("This method is abstract and must be implemented")
}

// Exit performs all the required operations to exit after a simulation.
func (c *component) Exit() {
	panic("Components must implement the Exit function to be valid")
}

// IsInputEmpty returns true if none of the input ports has messages.
func (c *component) IsInputEmpty() bool {
	for _, inPort := range c.inPorts {
		if !inPort.IsEmpty() {
			return false
		}
	}
	return true
}

// AddInPort adds new Port to the input Port list of the component.
func (c *component) AddInPort(port Port) {
	port.setParent(c)
	c.inPorts = append(c.inPorts, port)
}

// GetInPort returns input Port of the component with the requested name.
// if no input Port has the requested name, it returns a nil pointer and an error.
func (c *component) GetInPort(portName string) Port {
	return c.getPort(c.inPorts, portName)
}

// GetInPorts return a slice of all the input ports of the component.
func (c *component) GetInPorts() []Port {
	return c.inPorts
}

// AddOutPort adds new Port to the output Port list of the component.
func (c *component) AddOutPort(port Port) {
	port.setParent(c)
	c.outPorts = append(c.outPorts, port)
}

// GetOutPort returns output Port of the component with the requested name.
// if no output Port has the requested name, it returns a nil pointer.
func (c *component) GetOutPort(portName string) Port {
	return c.getPort(c.outPorts, portName)
}

// GetOutPorts return a slice of all the output ports of the component.
func (c *component) GetOutPorts() []Port {
	return c.outPorts
}

// setParent sets c as the parent DEVS component of component.
func (c *component) setParent(component Component) {
	c.parent = component
}

// GetParent returns the parent DEVS component of the component.
func (c *component) GetParent() Component {
	return c.parent
}

// String returns a string representation of the component.
func (c *component) String() string {
	name := c.name + ": "
	name += "Inports [ "
	for _, inPort := range c.inPorts {
		name += inPort.GetName() + " "
	}
	name += "] Outports [ "
	for _, outPort := range c.outPorts {
		name += outPort.GetName() + " "
	}
	name += "]"
	return name
}
