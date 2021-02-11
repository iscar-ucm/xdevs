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

package devstone

import (
	"github.com/pointlesssoft/godevs/examples/devstone"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/simulation"
	"github.com/pointlesssoft/godevs/pkg/util"
	"github.com/stretchr/testify/assert"
	"testing"
)

func ComputeExpectedComponents(t devstone.Topology, depth, width int) (nAtomic, nCoupled int) {
	switch t {
	case "HOmod":
		panic("implement me")
	default:
		nAtomic = 1 + (depth-1)*(width-1)
	}
	nCoupled = depth
	return
}

func ComputeExpectedCouplings(t devstone.Topology, depth, width int) (nIC, nEIC, nEOC int) {
	if width > 2 {
		switch t {
		case "HI", "HO":
			nIC = (depth - 1) * (width - 2)
		case "HOmod":
			panic("implement me")
		}
	}
	switch t {
	case "LI", "HI":
		nEIC = 1 + (depth-1)*width
		nEOC = depth
	case "HO":
		nEIC = 1 + (depth-1)*(width+1)
		nEOC = 1 + (depth-1)*width
	case "HOmod":
		panic("implement me")
	}
	return
}

func ComputeExpectedEvents(t devstone.Topology, depth, width int) (intCount, extCount int) {
	switch t {
	case "LI":
		intCount = 1 + (depth-1)*(width-1)
	case "HI":
		intCount = 1 + (width-1)*width/2*(depth-1)
	case "HO":
		intCount = 1 + (width-1)*width/2*(depth-1)
	case "HOmod":
		panic("implement me")
	}
	extCount = intCount
	return
}

const (
	MaxDepth = 10 // TODO increase it later
	MaxWidth = 10
)

func TestDEVStoneCornerCases(t *testing.T) {
	topologyError := devstone.TopologyError("")
	dimensionError := &devstone.DimensionError{}
	timingConfigError := &devstone.TimingConfigError{}

	var err error

	_, err = devstone.NewDEVStone("name", devstone.Topology("invalid"), -1, 0, -1, -1, -1)
	assert.IsType(t, topologyError, err, "Expected error of type %T, but got %T instead", topologyError, err)

	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), -1, -1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HI"), -1, -1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HO"), -1, -1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), -1, -1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)

	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 0, -1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 0, 0, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 0, 1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("LI"), 1, 1, -1, -1, -1)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 1, 1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 1, -1, -1, -1)
	assert.IsType(t, dimensionError, err, "Expected error of type %T, but got %T instead", dimensionError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, -1, -1)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)

	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, -1, -1)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, 0, -1)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, -1, 0)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, -1, 0, 0)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, -1, 0)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, 0, -1)
	assert.IsType(t, timingConfigError, err, "Expected error of type %T, but got %T instead", timingConfigError, err)
	_, err = devstone.NewDEVStone("name", devstone.Topology("HOmod"), 2, 2, 0, 0, 0)
	assert.Nil(t, err, "Expected nil error, but got %T instead", err)
}

func TestDEVStoneLI(t *testing.T) {
	topology := devstone.Topology("LI")
	for depth := 1; depth < MaxDepth; depth++ {
		for width := 1; width < MaxWidth; width++ {
			expectedAtomic, _ := ComputeExpectedComponents(topology, depth, width)
			expectedIC, expectedEIC, expectedEOC := ComputeExpectedCouplings(topology, depth, width)
			expectedIntCount, expectedExtCount := ComputeExpectedEvents(topology, depth, width)

			d, err := devstone.NewDEVStone("test_li", topology, depth, width, 0, 0, 0)
			assert.Nil(t, err)
			model, ok := d.(modeling.Coupled)
			assert.True(t, ok)
			nAtomic, nCoupled := model.CountComponents()
			nIC, nEIC, nEOC := model.CountCouplings()

			assert.Equal(t, expectedAtomic, nAtomic, "dimension: (%v,%v); expected atomics: %v; observed: %v", depth, width, expectedAtomic, nAtomic)
			assert.Equal(t, depth, nCoupled, "dimension: (%v,%v); expected coupled: %v; observed: %v", depth, width, depth, nCoupled)
			assert.Equal(t, expectedIC, nIC, "dimension: (%v,%v); expected IC: %v observed: %v", depth, width, expectedIC, nIC)
			assert.Equal(t, expectedEIC, nEIC, "dimension: (%v,%v); expected EIC: %v; observed: %v", depth, width, expectedEIC, nEIC)
			assert.Equal(t, expectedEOC, nEOC, "dimension: (%v,%v); expected EOC: %v; observed: %v", depth, width, expectedEOC, nEOC)

			coordinator := simulation.NewRootCoordinator(0, model)
			coordinator.Initialize()
			coordinator.SimInject(0, model.GetInPort("iIn"), []int{0})
			coordinator.SimulateTime(util.INFINITY)
			coordinator.Exit()

			intCount, extCount := d.GetEventCount()
			assert.Equal(t, expectedIntCount, intCount, "dimension: (%v,%v); expected internal events: %v; observed: %v", depth, width, expectedIntCount, intCount)
			assert.Equal(t, expectedExtCount, extCount, "dimension: (%v,%v); expected external events: %v; observed: %v", depth, width, expectedExtCount, extCount)
		}
	}
}

func TestDEVStoneHI(t *testing.T) {
	topology := devstone.Topology("HI")
	for depth := 1; depth < MaxDepth; depth++ {
		for width := 1; width < MaxWidth; width++ {
			expectedAtomic, _ := ComputeExpectedComponents(topology, depth, width)
			expectedIC, expectedEIC, expectedEOC := ComputeExpectedCouplings(topology, depth, width)
			expectedIntCount, expectedExtCount := ComputeExpectedEvents(topology, depth, width)

			d, err := devstone.NewDEVStone("test_hi", topology, depth, width, 0, 0, 0)
			assert.Nil(t, err)
			model, ok := d.(modeling.Coupled)
			assert.True(t, ok)
			nAtomic, nCoupled := model.CountComponents()
			nIC, nEIC, nEOC := model.CountCouplings()

			assert.Equal(t, expectedAtomic, nAtomic, "dimension: (%v,%v); expected atomics: %v; observed: %v", depth, width, expectedAtomic, nAtomic)
			assert.Equal(t, depth, nCoupled, "dimension: (%v,%v); expected coupled: %v; observed: %v", depth, width, depth, nCoupled)
			assert.Equal(t, expectedIC, nIC, "dimension: (%v,%v); expected IC: %v observed: %v", depth, width, expectedIC, nIC)
			assert.Equal(t, expectedEIC, nEIC, "dimension: (%v,%v); expected EIC: %v; observed: %v", depth, width, expectedEIC, nEIC)
			assert.Equal(t, expectedEOC, nEOC, "dimension: (%v,%v); expected EOC: %v; observed: %v", depth, width, expectedEOC, nEOC)

			coordinator := simulation.NewRootCoordinator(0, model)
			coordinator.Initialize()
			coordinator.SimInject(0, model.GetInPort("iIn"), []int{0})
			coordinator.SimulateTime(util.INFINITY)
			coordinator.Exit()

			intCount, extCount := d.GetEventCount()
			assert.Equal(t, expectedIntCount, intCount, "dimension: (%v,%v); expected internal events: %v; observed: %v", depth, width, expectedIntCount, intCount)
			assert.Equal(t, expectedExtCount, extCount, "dimension: (%v,%v); expected external events: %v; observed: %v", depth, width, expectedExtCount, extCount)
		}
	}
}

func TestDEVStoneHO(t *testing.T) {
	topology := devstone.Topology("HO")
	for depth := 1; depth < MaxDepth; depth++ {
		for width := 1; width < MaxWidth; width++ {
			expectedAtomic, _ := ComputeExpectedComponents(topology, depth, width)
			expectedIC, expectedEIC, expectedEOC := ComputeExpectedCouplings(topology, depth, width)
			expectedIntCount, expectedExtCount := ComputeExpectedEvents(topology, depth, width)

			d, err := devstone.NewDEVStone("test_hi", topology, depth, width, 0, 0, 0)
			assert.Nil(t, err)
			model, ok := d.(modeling.Coupled)
			assert.True(t, ok)
			nAtomic, nCoupled := model.CountComponents()
			nIC, nEIC, nEOC := model.CountCouplings()

			assert.Equal(t, expectedAtomic, nAtomic, "dimension: (%v,%v); expected atomics: %v; observed: %v", depth, width, expectedAtomic, nAtomic)
			assert.Equal(t, depth, nCoupled, "dimension: (%v,%v); expected coupled: %v; observed: %v", depth, width, depth, nCoupled)
			assert.Equal(t, expectedIC, nIC, "dimension: (%v,%v); expected IC: %v observed: %v", depth, width, expectedIC, nIC)
			assert.Equal(t, expectedEIC, nEIC, "dimension: (%v,%v); expected EIC: %v; observed: %v", depth, width, expectedEIC, nEIC)
			assert.Equal(t, expectedEOC, nEOC, "dimension: (%v,%v); expected EOC: %v; observed: %v", depth, width, expectedEOC, nEOC)

			coordinator := simulation.NewRootCoordinator(0, model)
			coordinator.Initialize()
			coordinator.SimInject(0, model.GetInPort("iIn"), []int{0})
			coordinator.SimulateTime(util.INFINITY)
			coordinator.Exit()

			intCount, extCount := d.GetEventCount()
			assert.Equal(t, expectedIntCount, intCount, "dimension: (%v,%v); expected internal events: %v; observed: %v", depth, width, expectedIntCount, intCount)
			assert.Equal(t, expectedExtCount, extCount, "dimension: (%v,%v); expected external events: %v; observed: %v", depth, width, expectedExtCount, extCount)
		}
	}
}
