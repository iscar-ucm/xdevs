package modeling

import (
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestPort(t *testing.T) {
	/* 1. Check that ports start empty, regardless of the initial slice */
	port1 := modeling.NewPort("integer_port", make([]int, 0))
	assert.True(t, port1.IsEmpty(), "Initially, port should be empty")
	port2 := modeling.NewPort("string_port", []string{"hello", "world"})
	assert.True(t, port2.IsEmpty(), "Initially, port should be empty")

	/* 2. Check that ports do not accept new values of a different type */
	assert.Panics(t, func() {port1.AddValue("invalid")}, "port of type %T should not admit values of type ", 0, "invalid")
	assert.Panics(t, func() {port2.AddValue(0)}, "port of type %T should not admit values of type ", "invalid", 0)

	/* 3. Check that ports accept new values of their corresponding type */
	port1.AddValue(1)
	port1.AddValues([]int{2, 3, 4})
	assert.Equal(t, 4, port1.Length(), "port size should be 4")
	port2.AddValue("Hello")
	port2.AddValues([]string{"World", "!"})
	assert.Equal(t, 3, port2.Length(), "port size should be 3")

	/* 3. Check that the GetSingleValue and GetValue functions works */
	assert.EqualValues(t, 1, port1.GetSingleValue().(int), "First element should be 1")
	assert.EqualValues(t, port1.Length(), len(port1.GetValues().([]int)), "Length should be the same")
	assert.EqualValues(t, "Hello", port2.GetSingleValue().(string), "First element should be \"Hello\"")
	assert.EqualValues(t, port2.Length(), len(port2.GetValues().([]string)), "Length should be the same")

	/* 4. Check that the Clear function works */
	port1.Clear()
	assert.True(t, port1.IsEmpty(), "After clearing it, port should be empty")
	assert.Panics(t, func() {port1.GetSingleValue()}, "As port is empty, it should panic")
	assert.Equal(t, 0, len(port1.GetValues().([]int)), "Slice should be empty")
	port2.Clear()
	assert.True(t, port2.IsEmpty(), "After clearing it, port should be empty")
	assert.Panics(t, func() { port2.GetSingleValue() }, "As port is empty, it should panic")
	assert.Equal(t, 0, len(port2.GetValues().([]string)), "Slice should be empty")

	/* 5. Check that the initial parent of ports is nil */
	assert.Nil(t, port1.GetParent(), "If port was not created by atomic model, parent should be nil")
	assert.Nil(t, port2.GetParent(), "If port was not created by atomic model, parent should be nil")
}
