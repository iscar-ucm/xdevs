package modeling

type Atomic interface {
	Component

}

func NewAtomic(name string) Atomic {
	c := atomic{NewComponent(name)}
	return &c
}

type atomic struct {
	Component
}