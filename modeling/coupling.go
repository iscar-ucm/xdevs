package modeling

type Coupling interface {
	String() string
	PropagateValues()
	GetPortFrom() *Port
	getPortTo() *Port
}

func NewCoupling(portFrom *Port, portTo *Port) Coupling {
	c := coupling{portFrom, portTo}
	return &c
}

type coupling struct {
	portFrom *Port
	portTo *Port
}

func (c *coupling) String() string {
	return "(" + (*c.portFrom).GetName() + "->" + (*c.portTo).GetName() + ")"
}

func (c *coupling) PropagateValues() {
	(*c.portTo).AddValues((*c.portFrom).GetValues())
}

func (c *coupling) GetPortFrom() *Port {
	return c.portFrom
}

func (c *coupling) getPortTo() *Port {
	return c.portTo
}
