package devstone

import (
	"github.com/pointlesssoft/godevs/examples/devstone"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestDEVStoneInvalidInput(t *testing.T) {
	var err error

	_, err = devstone.NewDEVStone("name", devstone.Topology("invalid"), -1, 0, -1, -1, -1)
	assert.IsType(t, devstone.TopologyError(""), err)

	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), -1, -1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HI"), -1, -1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HO"), -1, -1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), -1, -1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)

	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 0, -1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 0, 0, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 0, 1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 1, 1, -1, -1, -1)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 1, 1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 1, -1, -1, -1)
	assert.IsType(t, &devstone.DimensionError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, -1, -1)
	assert.IsType(t, &devstone.TimingConfigError{}, err)

	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, -1, -1)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, 0, -1)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, -1, 0)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, 0, 0)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, -1, 0)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, 0, -1)
	assert.IsType(t, &devstone.TimingConfigError{}, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, 0, 0)
	assert.Nil(t, err)
}

func TestDEVStoneLI(t *testing.T) {
	
}
