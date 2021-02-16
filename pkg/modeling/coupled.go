/*
 * Copyright (c) 2021, Román Cárdenas Rodríguez.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package modeling

type Coupled interface {
	Component                                 // Coupled interface complies with the Component interface.
	AddCoupling(pFrom Port, pTo Port)         // Creates a Coupling between two Ports.
	GetComponents() []Component               // Returns all the children Components of the Coupled model.
	GetComponentByName(name string) Component // Returns a child Component with a given name.
	AddComponent(comp Component)              // Adds a new child component.
	GetIC() []Coupling                        // Returns  the Internal Coupling list.
	GetEIC() []Coupling                       // Returns the External Input Coupling list.
	GetEOC() []Coupling                       // Returns the External Output Coupling list.
	CountComponents() (nAtomic, nCoupled int) // Returns number of components of the model broken down into types.
	CountCouplings() (nIC, nEIC, nEOC int)    // Returns number of couplings of the model broken down into types.
}

// NewCoupled returns a pointer to a structure that complies the Coupled interface.
// name: name of the Coupled model.
func NewCoupled(name string) Coupled {
	c := coupled{NewComponent(name), nil, nil, nil, nil}
	return &c
}

type coupled struct {
	Component                // It complies the Component interface.
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
// It panics if a parent component of any Port is not a child model of the Coupled model or the Coupled model itself.
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

// CountComponents returns the number of components in the model.
// The result is broken down into atomic (nAtomic) and coupled (nCoupled) models.
func (c *coupled) CountComponents() (nAtomic, nCoupled int) {
	nAtomic, nCoupled = 0, 1 // nCoupled is initially set to 1 to count c itself
	for _, component := range c.GetComponents() {
		if coupled, ok := component.(Coupled); ok {
			x, y := coupled.CountComponents()
			nAtomic, nCoupled = nAtomic+x, nCoupled+y
		} else {
			nAtomic++
		}
	}
	return
}

// CountCouplings returns the number of couplings in the model.
// The result is broken down into IC (nIC), EIC (nEIC), and EOC (nEOC) couplings.
func (c *coupled) CountCouplings() (nIC, nEIC, nEOC int) {
	nIC, nEIC, nEOC = len(c.ic), len(c.eic), len(c.eoc)
	for _, component := range c.GetComponents() {
		if coupled, ok := component.(Coupled); ok {
			x, y, z := coupled.CountCouplings()
			nIC, nEIC, nEOC = nIC+x, nEIC+y, nEOC+z
		}
	}
	return
}
