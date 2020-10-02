package modeling

import "reflect"

type Coupling interface {
	String() string
	PropagateValues()
	GetPortFrom() Port
	GetPortTo() Port
}

func NewCoupling(portFrom Port, portTo Port) Coupling {
	if reflect.TypeOf(portFrom.GetValues()) != reflect.TypeOf(portTo.GetValues()) {
		panic("Both ports of a coupling must be of the same type")
	}
	c := coupling{portFrom, portTo}
	return &c
}

type coupling struct {
	portFrom Port
	portTo Port
}

func (c *coupling) String() string {
	return "(" + c.portFrom.GetName() + "->" + c.portTo.GetName() + ")"
}

func (c *coupling) PropagateValues() {
	c.portTo.AddValues(c.portFrom.GetValues())
}

func (c *coupling) GetPortFrom() Port {
	return c.portFrom
}

func (c *coupling) GetPortTo() Port {
	return c.portTo
}
