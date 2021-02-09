package modeling

type Coupled interface {
	Component                                          // Coupled interface complies with the Component interface.
	AddCoupling(pFrom Port, pTo Port)                  // Creates a Coupling between two Ports.
	GetComponents() []Component                        // Returns all the children Components of the Coupled model.
	GetComponentByName(name string) Component 		   // Returns a child Component with a given name.
	AddComponent(comp Component)                       // Adds a new child component.
	GetIC() []Coupling                                 // Returns  the Internal Coupling list. TODO map?
	GetEIC() []Coupling                                // Returns the External Input Coupling list. TODO map?
	GetEOC() []Coupling                                // Returns the External Output Coupling list. TODO map?
}

// NewCoupled returns a pointer to a structure that complies the Coupled interface.
// name: name of the Coupled model.
func NewCoupled(name string) Coupled {
	c := coupled{NewComponent(name), nil, nil, nil, nil}
	return &c
}

type coupled struct {
	Component                // It complies the component interface.
	components   []Component // Slice of all the children Components. TODO map?
	ic, eic, eoc []Coupling  // List of couplings. TODO map? ic, eic, eoc map[Port]map[Port]Coupling
}

// Initialize
func (c *coupled) Initialize() {}

// Exit
func (c *coupled) Exit() {}

// AddCoupling adds a coupling to the coupled model. Ports' parent models must be theCoupled model or a child model.
// pFrom: source Port.
// pTo: destination Port.
// It panics if a parent component of any Port is not a child model of the Coupled model  or the Coupled model itself.
func (c *coupled) AddCoupling(pFrom Port, pTo Port) { // TODO check that the coupling is legit
	for _, p := range []Port{pFrom, pTo} {
		if p.GetParent() == nil {
			panic("port " + pFrom.String() + " does not have a parent component")
		}
	}
	coup := NewCoupling(pFrom, pTo)
	if pFrom.GetParent() == c.Component {
		c.eic = append(c.eic, coup)
	} else if pTo.GetParent() == c.Component {
		c.eoc = append(c.eoc, coup)
	} else {
		c.ic = append(c.ic, coup)
	}
}

// GetComponents returns a slice with all the children Components.
func (c *coupled) GetComponents() []Component {
	return c.components
}

// GetComponentByName returns a child Component with matching name. If no Component is found, it returns an error.
func (c *coupled) GetComponentByName(name string) Component {
	for _, c := range c.components {
		if c.GetName() == name {
			return c
		}
	}
	return nil
}

// AddComponent adds a new child component.
func (c *coupled) AddComponent(comp Component) {
	comp.setParent(c)
	c.components = append(c.components, comp)
}

//GetIC returns  the Internal Coupling list.
func (c *coupled) GetIC() []Coupling {
	return c.ic
}

// GetEIC returns the External Input Coupling list.
func (c *coupled) GetEIC() []Coupling {
	return c.eic
}

// GetEOC returns the External Output Coupling list.
func (c *coupled) GetEOC() []Coupling {
	return c.eoc
}
