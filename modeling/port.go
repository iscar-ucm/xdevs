package modeling

import "reflect"

type Port interface {
	GetName() string
	Length() int
	IsEmpty() bool
	Clear()
	AddValue(val interface{})
	AddValues(val interface{})
	GetSingleValue() interface{}
	GetValues() interface{}
	setParent(c Component)
	GetParent() Component
	String() string
}

func NewPort(name string, portValue interface{}) Port {
	switch reflect.ValueOf(portValue).Kind() {
	case reflect.Slice:
		p := port{name, nil, portValue}
		p.Clear()
		return &p
	default:
		panic("port Value must be of kind reflect.Slice")
	}
}

type port struct {
	name   string
	parent Component
	values interface{}
}

func (p *port) GetName() string {
	return p.name
}

func (p *port) Length() int {
	return reflect.ValueOf(p.values).Len()
}

func (p *port) IsEmpty() bool {
	return p.Length() == 0
}

func (p *port) Clear() {
	p.values = reflect.MakeSlice(reflect.TypeOf(p.values), 0, 0).Interface()
}

func (p *port) AddValue(val interface{}) {
	value := reflect.ValueOf(&p.values).Elem()
	value.Set(reflect.Append(reflect.ValueOf(p.values), reflect.ValueOf(val)))
	p.values = value.Interface()
}

func (p *port) AddValues(val interface{}) {
	value := reflect.ValueOf(&p.values).Elem()
	add := reflect.ValueOf(val)
	for i := 0; i < add.Len(); i++ {
		value.Set(reflect.Append(reflect.ValueOf(p.values), add.Index(i)))
	}
	p.values = value.Interface()
}

func (p *port) GetSingleValue() interface{} {
	return reflect.ValueOf(p.values).Index(0).Interface()
}

func (p *port) GetValues() interface{} {
	return p.values
}

func (p *port) setParent(c Component)  {
	p.parent = c
}

func (p *port) GetParent() Component {
	return p.parent
}

func (p *port) String() string {
	name := p.name
	auxComponent := p.parent
	for auxComponent != nil {
		name = auxComponent.GetName() + "." + name
		auxComponent = auxComponent.GetParent()
	}
	return name
}
