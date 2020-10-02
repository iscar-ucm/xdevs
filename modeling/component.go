package modeling

type ComponentInterface interface {
	GetName() string
	Initialize()
	Exit()
	IsInputEmpty() bool
	AddInPort(port *Port)
	GetInPort(portName string) *Port
	GetInPorts() []*Port
	AddOutPort(port *Port)
	GetOutPort(portName string) *Port
	GetOutPorts() []*Port
	GetParent() ComponentInterface
	setParent(component ComponentInterface)
	String() string
}

type Component struct {
	name     string
	parent   ComponentInterface
	inPorts  []*Port  	// TODO map?
	outPorts []*Port  	// TODO map?
}

func NewComponent(name string) *Component {
	c := Component{name, nil, nil, nil}
	return &c
}

func (c *Component) GetName() string {
	return c.name
}

func (c *Component) Initialize() {
	panic("This method is abstract and must be implemented")
}

func (c *Component) Exit() {
	panic("This method is abstract and must be implemented")
}

func (c *Component) IsInputEmpty() bool {
	for _, inPort := range c.inPorts {
		if ! inPort.IsEmpty() {
			return false
		}
	}
	return true
}

func (c *Component) AddInPort(port *Port) {
	port.setParent(c)
	c.inPorts = append(c.inPorts, port)
}

func (c *Component) GetInPort(portName string) *Port {
	for _, inPort := range c.inPorts {
		if inPort.GetName() == portName {
			return inPort
		}
	}
	panic("port name was not found within the input ports list")
}

func (c *Component) GetInPorts() []*Port {
	return c.inPorts
}

func (c *Component) AddOutPort(port *Port) {
	port.setParent(c)
	c.outPorts = append(c.outPorts, port)
}

func (c *Component) GetOutPort(portName string) *Port {
	for _, outPort := range c.outPorts {
		if outPort.GetName() == portName {
			return outPort
		}
	}
	panic("port name was not found within the output ports list")
}

func (c *Component) GetOutPorts() []*Port {
	return c.outPorts
}

func (c *Component) GetParent() ComponentInterface {
	return c.parent
}

func (c *Component) setParent(component ComponentInterface) {
	c.parent = component
}

func (c *Component) String() string {
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
