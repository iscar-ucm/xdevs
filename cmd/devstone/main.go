package main

import (
	"flag"
	"fmt"
	"github.com/pointlesssoft/godevs/examples/devstone"
	"github.com/pointlesssoft/godevs/pkg/simulation"
	"github.com/pointlesssoft/godevs/pkg/util"
	"os"
	"time"
)

func isFlagPassed(name string) bool {
	found := false
	flag.Visit(func(f *flag.Flag) {
		if f.Name == name {
			found = true
		}
	})
	return found
}

func main() {
	var name, topology string
	var depth, width uint64
	var intDelay, extDelay, prepTime float64

	flag.StringVar(&name, "n", "devstone", "name of DEVStone model")
	flag.StringVar(&topology, "t", "", "topology of the DEVStone model (required)")
	flag.Uint64Var(&depth, "d", 0, "depth of the top DEVStone model (required)")
	flag.Uint64Var(&width, "w", 0, "width of the top DEVStone model (required)")
	flag.Float64Var(&intDelay, "i", 0, "internal delay of atomic models")
	flag.Float64Var(&extDelay, "e", 0, "external delay of atomic models")
	flag.Float64Var(&prepTime, "p", 0, "preparation time of atomic models")
	flag.Parse()

	if topology == "" || depth == 0 || width == 0 {
		flag.PrintDefaults()
		os.Exit(1)
	}

	fmt.Printf("name: %v, topology: %v, depth: %v, width: %v, intDelay: %v, extDelay: %v, prepTime: %v\n",
		name, topology, depth, width, intDelay, extDelay, prepTime)

	start := time.Now()
	model := devstone.NewDEVStone(name, devstone.Topology(topology), depth, width, intDelay, extDelay, prepTime)
	elapsedModel := time.Since(start)
	fmt.Printf("Model creation time: %v\n", elapsedModel)

	coordinator := simulation.NewRootCoordinator(0, model)
	coordinator.Initialize()
	elapsedEngine := time.Since(start) - elapsedModel
	fmt.Printf("Engine setup time: %v\n", elapsedEngine)

	coordinator.SimulateTime(util.INFINITY)
	elapsedSimulation := time.Since(start) - elapsedEngine
	coordinator.Exit()
	fmt.Printf("Simulation time: %v\n", elapsedSimulation)
}
