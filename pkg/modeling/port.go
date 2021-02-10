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

import "reflect"

type Port interface {
	GetName() string             // returns the name of the Port.
	Length() int                 // returns the number of elements stored in the Port.
	IsEmpty() bool               // returns true if the Port does not contain any element.
	Clear()                      // removes all the elements stored in the Port.
	AddValue(val interface{})    // adds a single value to the Port.
	AddValues(val interface{})   // Add multiple values to the Port. Values must be stored in a slice.
	GetSingleValue() interface{} // returns a single value of the Port.
	GetValues() interface{}      // returns a slice with all the values of the Port.
	setParent(c Component)       // sets a given DEVS model as parent of the Port.
	GetParent() Component        // returns the parent of the Port.
	String() string              // returns a string representation of the Port.
}

// NewPort returns a pointer to a structure that complies the Port interface.
// name: name of the port.
// portValue: slice of desired the data type of the port.
// It panics if portValue is not a slice.
func NewPort(name string, portValue interface{}) Port {
	switch reflect.ValueOf(portValue).Kind() {
	case reflect.Slice:
		p := port{name, nil, portValue}
		p.Clear()
		return &p
	default:
		panic("port Value must be of kind reflect.Slice")
	}
}

type port struct {
	name   string      // Name of the port.
	parent Component   // Parent DEVS model that contains the port.
	values interface{} // Values stored in the port. It is an empty interface to allow ports of different data types.
}

// GetName returns the name of the port.
func (p *port) GetName() string {
	return p.name
}

// Length returns the number of values stored in the port.
func (p *port) Length() int {
	return reflect.ValueOf(p.values).Len()
}

// IsEmpty returns true if the port does not have any stored value.
func (p *port) IsEmpty() bool {
	return p.Length() == 0
}

// Clear removes all the values stored in the port.
func (p *port) Clear() {
	p.values = reflect.MakeSlice(reflect.TypeOf(p.values), 0, 0).Interface()
}

// AddValue adds a single value to the port.
func (p *port) AddValue(val interface{}) {
	value := reflect.ValueOf(&p.values).Elem()
	value.Set(reflect.Append(reflect.ValueOf(p.values), reflect.ValueOf(val)))
	p.values = value.Interface()
}

// AddValues adds multiple values to the port. Values must be provided in a slice.
func (p *port) AddValues(val interface{}) {
	value := reflect.ValueOf(&p.values).Elem()
	add := reflect.ValueOf(val)
	for i := 0; i < add.Len(); i++ {
		value.Set(reflect.Append(reflect.ValueOf(p.values), add.Index(i)))
	}
	p.values = value.Interface()
}

// GetSingleValue returns the first value stored in the port.
// It panics if port is empty (i.e., index 0 is out of range).
func (p *port) GetSingleValue() interface{} {
	return reflect.ValueOf(p.values).Index(0).Interface()
}

// GetValues returns a slice containing all the values stored in the port.
func (p *port) GetValues() interface{} {
	return p.values
}

// setParent sets c as the parent DEVS Component of the port.
func (p *port) setParent(c Component) {
	p.parent = c
}

// GetParent returns the parent component of the port.
func (p *port) GetParent() Component {
	return p.parent
}

// String returns a string representation of the port.
func (p *port) String() string {
	name := p.name
	auxComponent := p.parent
	for auxComponent != nil {
		name = auxComponent.GetName() + "." + name
		auxComponent = auxComponent.GetParent()
	}
	return name
}
