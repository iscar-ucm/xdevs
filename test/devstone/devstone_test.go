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
	"fmt"
	"github.com/pointlesssoft/godevs/examples/devstone"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/simulation"
	"github.com/pointlesssoft/godevs/pkg/util"
	"github.com/stretchr/testify/assert"
	"testing"
)

const (
	MaxDepth = 20 // TODO increase it later
	MaxWidth = 20
)

func ComputeExpectedComponents(t devstone.Topology, depth, width int) (nAtomic, nCoupled int) {
	switch t {
	case "HOmod":
		nAtomic = 1 + (depth-1)*(width-1+width*(width-1)/2)
	default:
		nAtomic = 1 + (depth-1)*(width-1)
	}
	nCoupled = depth
	return
}

func ComputeExpectedCouplings(t devstone.Topology, depth, width int) (nEIC, nIC, nEOC int) {
	switch t {
	/*
		case "LI", "HI":
			nEIC = 1 + (depth-1)*width
		case "HO":
			nEIC = 1 + (depth-1)*(width+1)
		case "HOmod":
			nEIC = 1 + (depth-1)*(1+2*(width-1))
	*/
	case "HOmod":
		nEIC = 1 + (depth-1)*(1+2*(width-1))
	default:
		nEIC = 1 + (depth-1)*width
	}

	if width > 1 {
		switch t {
		case "HI", "HO":
			nIC = (depth - 1) * (width - 2)
		case "HOmod":
			nIC = (depth - 1) * ((width-1)*(width-1) + (width-1)*width/2)
		}
	}

	switch t {
	case "HO":
		nEOC = 1 + (depth-1)*width
	default:
		nEOC = depth
	}
	return
}

func ComputeExpectedEvents(t devstone.Topology, depth, width int) (eventCount int) {
	switch t {
	case "LI":
		eventCount = 1 + (depth - 1) * (width - 1)
	case "HI", "HO":
		eventCount = 1 + (depth - 1) * (width - 1)*width/2
	case "HOmod":
		for i := 1; i < depth; i++ {  // Atomics that belong to top and intermediate coupled models
			numInputs := 1 + (i - 1) * width
			transFirstRow := (width - 1) * (numInputs + width - 1)
			transOtherRows := numInputs * (width - 1)*width/2
			eventCount += transFirstRow + transOtherRows
		}
		eventCount += 1 + (depth - 1) * width  // Innermost atomic
	}
	return
}

func TestDEVStone(t *testing.T) {
	t.Run("corner-cases", func(t *testing.T) { DEVStoneCornerCasesTest(t) })
	t.Run("models", func(t *testing.T) {
		t.Run("LI", func(t *testing.T) {
			DEVStoneModelTest(t, "LI")
		})
		t.Run("HI", func(t *testing.T) {
			DEVStoneModelTest(t, "HI")
		})
		t.Run("HO", func(t *testing.T) {
			DEVStoneModelTest(t, "HO")
		})
		t.Run("HOmod", func(t *testing.T) {
			DEVStoneModelTest(t, "HOmod")
		})
	})
}

func DEVStoneCornerCasesTest(t *testing.T) {
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

func DEVStoneModelTest(t *testing.T, topology devstone.Topology) {
	for depth := 1; depth < MaxDepth; depth++ {
		initialWidth := 1
		if topology == "HOmod" {
			initialWidth = 2
		}
		for width := initialWidth; width < MaxWidth; width++ {
			testName := fmt.Sprintf("%v_%v_%v", topology, depth, width)
			t.Run(testName, func(t *testing.T) {
				expectedAtomic, _ := ComputeExpectedComponents(topology, depth, width)
				expectedEIC, expectedIC, expectedEOC := ComputeExpectedCouplings(topology, depth, width)
				expectedEvents := ComputeExpectedEvents(topology, depth, width)

				d, err := devstone.NewDEVStone(testName, topology, depth, width, 0, 0, 0)
				assert.Nil(t, err, "topology: %v; dimension: (%v,%v); failed to create DEVStone model", topology, depth, width)
				model, ok := d.(modeling.Coupled)
				assert.True(t, ok, "topology: %v; dimension: (%v,%v); failed to create DEVStone model", topology, depth, width)
				nAtomic, nCoupled := model.CountComponents()
				nIC, nEIC, nEOC := model.CountCouplings()

				assert.Equal(t, expectedAtomic, nAtomic, "topology: %v; dimension: (%v,%v); expected atomics: %v; observed: %v", topology, depth, width, expectedAtomic, nAtomic)
				assert.Equal(t, depth, nCoupled, "topology: %v; dimension: (%v,%v); expected coupled: %v; observed: %v", topology, depth, width, depth, nCoupled)
				assert.Equal(t, expectedEIC, nEIC, "topology: %v; dimension: (%v,%v); expected EIC: %v; observed: %v", topology, depth, width, expectedEIC, nEIC)
				assert.Equal(t, expectedIC, nIC, "topology: %v; dimension: (%v,%v); expected IC: %v observed: %v", topology, depth, width, expectedIC, nIC)
				assert.Equal(t, expectedEOC, nEOC, "topology: %v; dimension: (%v,%v); expected EOC: %v; observed: %v", topology, depth, width, expectedEOC, nEOC)

				coordinator := simulation.NewRootCoordinator(0, model)
				coordinator.Initialize()
				coordinator.SimInject(0, model.GetInPort("iIn"), []int{0})
				coordinator.SimulateTime(util.INFINITY)
				coordinator.Exit()

				eventCount := d.GetEventCount()
				assert.Equal(t, expectedEvents, eventCount, "topology: %v; dimension: (%v,%v); expected events: %v; observed: %v", topology, depth, width, expectedEvents, eventCount)
			})

		}
	}
}
