package simulation

type Clock interface {
	GetTime() float64
	SetTime(time float64)
}

func NewClock(t float64) Clock {
	return &clock{t}
}

type clock struct {
	time float64
}

func(c *clock) GetTime() float64 {
	return c.time
}

func(c *clock) SetTime(time float64) {
	c.time = time
}
