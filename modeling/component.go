package modeling

type Component interface {
	GetName() string
	Initialize()
	Exit()
	Clear()
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
	panic("implement me")
}

func (c *component) Initialize() {
	panic("implement me")
}

func (c *component) Exit() {
	panic("implement me")
}

func (c *component) Clear() {
	panic("implement me")
}

func (c *component) IsInputEmpty() bool {
	panic("implement me")
}

func (c *component) AddInPort(port *Port) {
	panic("implement me")
}

func (c *component) GetInPort(portName string) *Port {
	panic("implement me")
}

func (c *component) GetInPorts() []*Port {
	panic("implement me")
}

func (c *component) AddOutPort(port *Port) {
	panic("implement me")
}

func (c *component) GetOutPort(portName string) *Port {
	panic("implement me")
}

func (c *component) GetOutPorts() []*Port {
	panic("implement me")
}

func (c *component) GetParent() *Component {
	panic("implement me")
}

func (c *component) SetParent(component *Component) {
	panic("implement me")
}

func (c *component) ToString() string {
	panic("implement me")
}
