package simulation

type Clock interface {
	GetTime() float64		// Returns current simulation time.
	SetTime(time float64)	// Sets simulation time.
}

// Returns pointer to a structure that complies the Clock interface.
func NewClock(t float64) Clock {
	return &clock{t}
}

type clock struct {
	time float64	// Simulation time.
}

// GetTime returns current simulation time.
func(c *clock) GetTime() float64 {
	return c.time
}

// SetTime sets simulation time.
func(c *clock) SetTime(time float64) {
	c.time = time
}
