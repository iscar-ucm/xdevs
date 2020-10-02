package simulation

type Clock struct {
	time float64
}

func NewSimulationClock(t float64) *Clock {
	c := Clock{t}
	return &c
}

func(c *Clock) GetTime() float64 {
	return c.time
}

func(c *Clock) SetTime(time float64) {
	c.time = time
}
