package modeling

import (
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestCoupling(t *testing.T) {
	/* 1. Check that couplings between ports of different value type produce panics */
	port1From := modeling.NewPort("integer_port_from", make([]int, 0))
	port2From := modeling.NewPort("string_port_from", make([]string, 0))
	assert.Panics(t, func() { modeling.NewCoupling(port1From, port2From) })
	port1To := modeling.NewPort("integer_port_from", make([]int, 0))
	port2To := modeling.NewPort("string_port_from", make([]string, 0))

	/* 2. Check that couplings between ports of same value type are created with no problems */
	var coupling1, coupling2 modeling.Coupling
	assert.NotPanics(t, func() { coupling1 = modeling.NewCoupling(port1From, port1To) })
	assert.NotPanics(t, func() { coupling2 = modeling.NewCoupling(port2From, port2To) })

	/* 3. Check that ports are well positioned */
	assert.EqualValues(t, coupling1.GetPortFrom(), port1From)
	assert.Equal(t, coupling2.GetPortFrom(), port2From)
	assert.Equal(t, coupling1.GetPortTo(), port1To)
	assert.Equal(t, coupling2.GetPortTo(), port2To)

	/* 4. Check that values are propagated from port_from to port_to */
	port1From.AddValue(1)
	port2From.AddValue("hello")
	coupling1.PropagateValues()
	coupling2.PropagateValues()
	assert.Equal(t, 1, port1From.Length())
	assert.Equal(t, 1, port1To.Length())
	assert.Equal(t, 1, port2From.Length())
	assert.Equal(t, 1, port2To.Length())
	assert.Equal(t, port1From.GetSingleValue(), port1To.GetSingleValue())
	assert.Equal(t, port2From.GetSingleValue(), port2To.GetSingleValue())

	/* 5. Check that, when clearing source ports, destination ports keep the values */
	port1From.Clear()
	port2From.Clear()
	assert.Equal(t, 0, port1From.Length())
	assert.Equal(t, 1, port1To.Length())
	assert.Equal(t, 0, port2From.Length())
	assert.Equal(t, 1, port2To.Length())

	/* 6. Add new values to source, propagate, and assert that previous values remain */
	port1From.AddValues([]int{2, 3})
	port2From.AddValues([]string{"world", "!"})
	coupling1.PropagateValues()
	coupling2.PropagateValues()
	assert.Equal(t, 1+port1From.Length(), port1To.Length())
	assert.Equal(t, 1+port2From.Length(), port2To.Length())

	/* 7. Check that, when clearing destination ports, source ports keep the values */
	port1To.Clear()
	port2To.Clear()
	assert.Equal(t, 2, port1From.Length())
	assert.Equal(t, 0, port1To.Length())
	assert.Equal(t, 2, port2From.Length())
	assert.Equal(t, 0, port2To.Length())

	/* 8. Propagate values twice. Assert destination ports have two times more messages */
	coupling1.PropagateValues()
	coupling2.PropagateValues()
	coupling1.PropagateValues()
	coupling2.PropagateValues()
	assert.Equal(t, 2*port1From.Length(), port1To.Length())
	assert.Equal(t, 2*port2From.Length(), port2To.Length())

	/* 9. Clear all ports and assert they are empty */
	port1To.Clear()
	port2To.Clear()
	port1From.Clear()
	port2From.Clear()
	assert.True(t, port1To.IsEmpty())
	assert.True(t, port2To.IsEmpty())
	assert.True(t, port1From.IsEmpty())
	assert.True(t, port2From.IsEmpty())
}
