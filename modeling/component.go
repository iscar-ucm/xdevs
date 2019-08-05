package modeling

type Component interface {
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
	GetParent() *Component
	SetParent(component *Component)
	ToString() string
}

func NewComponent(name string) Component {
	c := component{name, nil, nil, nil}
	return &c
}

type component struct {
	Name string
	Parent *Component
	InPorts []*Port
	OutPorts []*Port
}

func (c *component) GetName() string {
	return c.Name
}

func (c *component) Initialize() {
	panic("This method is abstract and must be implemented")
}

func (c *component) Exit() {
	panic("This method is abstract and must be implemented")
}

func (c *component) IsInputEmpty() bool {
	res := true
	for _, inPort := range c.InPorts {
		if ! (*inPort).IsEmpty() {
			res = false
			break
		}
	}
	return res
}

func (c *component) AddInPort(port *Port) {
	c.InPorts = append(c.InPorts, port)
}

func (c *component) GetInPort(portName string) *Port {
	for _, inPort := range c.InPorts {
		if (*inPort).GetName() == portName {
			return inPort
		}
	}
	panic("port name was not found within the input ports list")
}

func (c *component) GetInPorts() []*Port {
	return c.InPorts
}

func (c *component) AddOutPort(port *Port) {
	c.OutPorts = append(c.OutPorts, port)
}

func (c *component) GetOutPort(portName string) *Port {
	for _, outPort := range c.OutPorts {
		if (*outPort).GetName() == portName {
			return outPort
		}
	}
	panic("port name was not found within the output ports list")
}

func (c *component) GetOutPorts() []*Port {
	return c.OutPorts
}

func (c *component) GetParent() *Component {
	return c.Parent
}

func (c *component) SetParent(component *Component) {
	c.Parent = component
}

func (c *component) ToString() string {
	name := c.Name + ": "
	name += "Inports[ "
	for _, inPort := range c.InPorts {
		name += (*inPort).GetName() + " "
	}
	name += "] Outports [ "
	for _, outPort := range c.OutPorts {
		name += (*outPort).GetName() + " "
	}
	name += "]"
	return name
}
