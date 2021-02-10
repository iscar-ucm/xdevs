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
