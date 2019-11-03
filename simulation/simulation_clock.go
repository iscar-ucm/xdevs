package simulation

type SimulationClock interface {
	GetTime() float64
	SetTime(time float64)
}

func NewSimulationClock(t float64) SimulationClock {
	c := simulationClock{t}
	return &c
}

type simulationClock struct {
	time float64
}

func(c *simulationClock) GetTime() float64 {
	return c.time
}

func(c *simulationClock) SetTime(time float64) {
	c.time = time
}
