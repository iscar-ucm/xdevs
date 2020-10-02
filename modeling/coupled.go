package modeling

type CoupledInterface interface {
	ComponentInterface
	AddCoupling(pFrom *Port, pTo *Port)
	GetComponent() ComponentInterface
	GetComponents() []ComponentInterface
	GetComponentByName(name string) ComponentInterface
	AddComponent(comp ComponentInterface)
	GetIC() []*Coupling
	GetEIC() []*Coupling
	GetEOC() []*Coupling
}

type Coupled struct {
	Component
	components []ComponentInterface
	ic, eic, eoc []*Coupling  // TODO map?
}

func NewCoupled(name string) *Coupled {
	c := Coupled{*NewComponent(name), nil, nil, nil, nil}
	return &c
}



func (c *Coupled) Initialize() { }

func (c *Coupled) Exit() { }

func (c *Coupled) AddCoupling(pFrom *Port, pTo *Port) {
	for _, p := range []*Port{pFrom, pTo} {
		if p.GetParent() == nil {
			panic("Port " + pFrom.String()  + " does not have a parent component")
		}
 	}
	coup := NewCoupling(pFrom, pTo)
	if pFrom.GetParent() == &c.Component {
		c.eic = append(c.eic, coup)
	} else if pTo.GetParent() == & c.Component {
		c.eoc = append(c.eoc, coup)
	} else {
		c.ic = append(c.ic, coup)
	}
}

func (c *Coupled) GetComponent() ComponentInterface {
	return &c.Component
}

func (c *Coupled) GetComponents() []ComponentInterface {
	return c.components
}

func (c *Coupled) GetComponentByName(name string) ComponentInterface {
	for _, c := range c.components {
		if c.GetName() == name {
			return c
		}
	}
	panic("no child component has name " + name)
}

func (c *Coupled) AddComponent(comp ComponentInterface) {
	comp.setParent(&c.Component)
	c.components = append(c.components, comp)
}

func (c *Coupled) GetIC() []*Coupling {
	return c.ic
}

func (c *Coupled) GetEIC() []*Coupling {
	return c.eic
}

func (c *Coupled) GetEOC() []*Coupling {
	return c.eoc
}
