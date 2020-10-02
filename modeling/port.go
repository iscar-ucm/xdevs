package modeling

import "reflect"

type Port struct {
	name   string
	parent ComponentInterface
	values interface{}
}

func NewPort(name string, portValue interface{}) *Port {
	switch reflect.ValueOf(portValue).Kind() {
	case reflect.Slice:
		p := Port{name, nil, portValue}
		p.Clear()
		return &p
	default:
		panic("Port Value must be of kind reflect.Slice")
	}
}



func (p *Port) GetName() string {
	return p.name
}

func (p *Port) Length() int {
	return reflect.ValueOf(p.values).Len()
}

func (p *Port) IsEmpty() bool {
	return p.Length() == 0
}

func (p *Port) Clear() {
	p.values = reflect.MakeSlice(reflect.TypeOf(p.values), 0, 0).Interface()
}

func (p *Port) AddValue(val interface{}) {
	value := reflect.ValueOf(&p.values).Elem()
	value.Set(reflect.Append(reflect.ValueOf(p.values), reflect.ValueOf(val)))
	p.values = value.Interface()
}

func (p *Port) AddValues(val interface{}) {
	value := reflect.ValueOf(&p.values).Elem()
	add := reflect.ValueOf(val)
	for i := 0; i < add.Len(); i++ {
		value.Set(reflect.Append(reflect.ValueOf(p.values), add.Index(i)))
	}
	p.values = value.Interface()
}

func (p *Port) GetSingleValue() interface{} {
	return reflect.ValueOf(p.values).Index(0).Interface()
}

func (p *Port) GetValues() interface{} {
	return p.values
}

func (p *Port) setParent(c ComponentInterface)  {
	p.parent = c
}

func (p *Port) GetParent() ComponentInterface {
	return p.parent
}

func (p *Port) String() string {
	name := p.name
	auxComponent := p.parent
	for auxComponent != nil {
		name = auxComponent.GetName() + "." + name
		auxComponent = auxComponent.GetParent()
	}
	return name
}
