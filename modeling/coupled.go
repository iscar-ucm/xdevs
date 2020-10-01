package modeling

type Coupled interface {
	Component
	AddCoupling(pFrom *Port, pTo *Port)
	GetComponent() *Component
	GetComponents() []*Component
	GetComponentByName(name string) *Component
	AddComponent(comp *Component)
	GetIC() []*Coupling
	GetEIC() []*Coupling
	GetEOC() []*Coupling
}

func NewCoupled(name string) Coupled {
	c := coupled{NewComponent(name), nil, nil, nil, nil}
	return &c
}

type coupled struct {
	Component
	components []*Component
	ic, eic, eoc []*Coupling
}

func (c *coupled) Initialize() { }

func (c *coupled) Exit() { }

func (c *coupled) AddCoupling(pFrom *Port, pTo *Port) {
	for _, p := range []*Port{pFrom, pTo} {
		if (*p).GetParent() == nil {
			panic("Port " + (*pFrom).String()  + " does not have a parent component")
		}
 	}
	coup := NewCoupling(pFrom, pTo)
	if (*pFrom).GetParent() == &c.Component {
		c.eic = append(c.eic, &coup)
	} else if (*pTo).GetParent() == & c.Component {
		c.eoc = append(c.eoc, &coup)
	} else {
		c.ic = append(c.ic, &coup)
	}
}

func (c *coupled) GetComponent() *Component {
	return &c.Component
}

func (c *coupled) GetComponents() []*Component {
	return c.components
}

func (c *coupled) GetComponentByName(name string) *Component {
	for _, c := range c.components {
		if (*c).GetName() == name {
			return c
		}
	}
	panic("no child component has name " + name)
}

func (c *coupled) AddComponent(comp *Component) {
	(*comp).SetParent(&c.Component)
	c.components = append(c.components, comp)
}

func (c *coupled) GetIC() []*Coupling {
	return c.ic
}

func (c *coupled) GetEIC() []*Coupling {
	return c.eic
}

func (c *coupled) GetEOC() []*Coupling {
	return c.eoc
}
