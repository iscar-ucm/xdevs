package modeling

type Component interface {
	GetName() string
	Initialize()
	Exit()
	IsInputEmpty() bool
	AddInPort(port Port)
	GetInPort(portName string) Port
	GetInPorts() []Port
	AddOutPort(port Port)
	GetOutPort(portName string) Port
	GetOutPorts() []Port
	GetParent() Component
	setParent(component Component)
	String() string
}

func NewComponent(name string) Component {
	c := component{name, nil, nil, nil}
	return &c
}

type component struct {
	name     string
	parent   Component
	inPorts  []Port // TODO map?
	outPorts []Port // TODO map?
}

func (c *component) GetName() string {
	return c.name
}

func (c *component) Initialize() {
	panic("This method is abstract and must be implemented")
}

func (c *component) Exit() {
	panic("This method is abstract and must be implemented")
}

func (c *component) IsInputEmpty() bool {
	for _, inPort := range c.inPorts {
		if ! inPort.IsEmpty() {
			return false
		}
	}
	return true
}

func (c *component) AddInPort(port Port) {
	port.setParent(c)
	c.inPorts = append(c.inPorts, port)
}

func (c *component) GetInPort(portName string) Port {
	for _, inPort := range c.inPorts {
		if inPort.GetName() == portName {
			return inPort
		}
	}
	panic("port name was not found within the input ports list")
}

func (c *component) GetInPorts() []Port {
	return c.inPorts
}

func (c *component) AddOutPort(port Port) {
	port.setParent(c)
	c.outPorts = append(c.outPorts, port)
}

func (c *component) GetOutPort(portName string) Port {
	for _, outPort := range c.outPorts {
		if outPort.GetName() == portName {
			return outPort
		}
	}
	panic("port name was not found within the output ports list")
}

func (c *component) GetOutPorts() []Port {
	return c.outPorts
}

func (c *component) GetParent() Component {
	return c.parent
}

func (c *component) setParent(component Component) {
	c.parent = component
}

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
