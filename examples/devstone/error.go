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

import "fmt"

type TopologyError string

func (t TopologyError) Error() string {
	return fmt.Sprintf("topology error (%s)", t)
}

func checkTopology(t Topology) error {
	switch t {
	case LI, HI, HO, HOmod:
		return nil
	}
	return TopologyError(t)
}

type DimensionError struct {
	Topology Topology
	Field    string
	Value    int
}

func (d *DimensionError) Error() string {
	return fmt.Sprintf("dimension error (topology: %s, field: %s, value: %d)", d.Topology, d.Field, d.Value)
}

func checkShape(topology Topology, depth, width int) error {
	if err := checkTopology(topology); err != nil {
		return err
	}
	if depth < 1 {
		return &DimensionError{topology, "depth", depth}
	} else {
		minimumWidth := 1
		if topology == HOmod {
			minimumWidth = 2
		}
		if width < minimumWidth {
			return &DimensionError{topology, "width", width}
		}
	}
	return nil
}

type TimingConfigError struct {
	Field string
	Value float64
}

func (t *TimingConfigError) Error() string {
	return fmt.Sprintf("timing config error (field: %s, value: %f)", t.Field, t.Value)
}

func checkTiming(intDelay, extDelay, prepTime float64) error {
	if intDelay < 0 {
		return &TimingConfigError{"intDelay", intDelay}
	} else if extDelay < 0 {
		return &TimingConfigError{"extDelay", extDelay}
	} else if prepTime < 0 {
		return &TimingConfigError{"prepTime", prepTime}
	}
	return nil
}
