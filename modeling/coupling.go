package modeling

import "reflect"

type Coupling struct {
	portFrom *Port
	portTo *Port
}

func NewCoupling(portFrom *Port, portTo *Port) *Coupling {
	if reflect.TypeOf(portFrom.GetValues()) != reflect.TypeOf(portTo.GetValues()) {
		panic("Both ports of a coupling must be of the same type")
	}
	c := Coupling{portFrom, portTo}
	return &c
}

func (c *Coupling) String() string {
	return "(" + c.portFrom.GetName() + "->" + c.portTo.GetName() + ")"
}

func (c *Coupling) PropagateValues() {
	c.portTo.AddValues(c.portFrom.GetValues())
}

func (c *Coupling) GetPortFrom() *Port {
	return c.portFrom
}

func (c *Coupling) GetPortTo() *Port {
	return c.portTo
}
