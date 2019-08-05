package modeling

import "reflect"

type Port interface {
	GetName() string
	Clear()
	IsEmpty() bool
	AddValue(val interface{})
	AddValues(val interface{})
	GetSingleValue() interface{}
	GetValues() interface{}
	GetParent() *Component
	ToString() string
}

func NewPort(name string, portValue interface{}) Port {
	switch reflect.ValueOf(portValue).Kind() {
	case reflect.Slice:
		p := port{Name: name, Parent:nil, Values:portValue}
		// p.Clear()
		return &p
	default:
		panic("Port Value must be of kind reflect.Slice")
	}
}

type port struct {
	Name string
	Parent *Component
	Values interface{}
}

func (p *port) IsEmpty() bool {
	return reflect.ValueOf(p.Values).Len() == 0
}

func (p *port) AddValue(val interface{}) {
	value := reflect.ValueOf(&p.Values).Elem()
	value.Set(reflect.Append(reflect.ValueOf(p.Values), reflect.ValueOf(val)))
	p.Values = value.Interface()
}

func (p *port) AddValues(val interface{}) {
	value := reflect.ValueOf(&p.Values).Elem()
	add := reflect.ValueOf(val)
	for i := 0; i < add.Len(); i++ {
		value.Set(reflect.Append(reflect.ValueOf(p.Values), add.Index(i)))
	}
	p.Values = value.Interface()
}

func (p *port) GetSingleValue() interface{} {
	return reflect.ValueOf(p.Values).Index(0)
}

func (p *port) GetValues() interface{} {
	return reflect.ValueOf(p.Values).Interface()
}

func (p *port) GetParent() *Component {
	return p.Parent
}

func (p *port) ToString() string {  // TODO implement this function
	panic("implement me")
}

func (p *port) GetName() string {
	return p.Name
}

func (p *port) Clear() {
	p.Values = reflect.MakeSlice(reflect.TypeOf(p.Values), 0, 0).Interface()
}
