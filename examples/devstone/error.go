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
