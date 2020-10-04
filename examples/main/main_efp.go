package main

import (
	"github.com/pointlesssoft/godevs/examples/efp"
	"github.com/pointlesssoft/godevs/pkg/simulation"
	"github.com/pointlesssoft/godevs/pkg/util"
)

func main() {
	model := efp.NewEFP("efp", 1, 3, 100)
	coordinator := simulation.NewRootCoordinator(0, model)
	coordinator.Initialize()
	coordinator.SimulateTime(util.INFINITY)
	coordinator.Exit()
}
