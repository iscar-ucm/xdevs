package main

import (
	"flag"
	"fmt"
	"github.com/pointlesssoft/godevs/examples/devstone"
	"github.com/pointlesssoft/godevs/pkg/modeling"
	"github.com/pointlesssoft/godevs/pkg/simulation"
	"github.com/pointlesssoft/godevs/pkg/util"
	"os"
	"time"
)

func printHelpAndExit() {
	fmt.Println("Use the following flags to configure the DEVStone model under study:")
	flag.PrintDefaults()
	os.Exit(1)
}

func main() {
	var name, topology string
	var depth, width int
	var intDelay, extDelay, prepTime float64

	flag.StringVar(&name, "n", "devstone", "name of DEVStone model")
	flag.StringVar(&topology, "t", "", "topology of the DEVStone model (required)")
	flag.IntVar(&depth, "d", 0, "depth of the top DEVStone model")
	flag.IntVar(&width, "w", 0, "width of the top DEVStone model")
	flag.Float64Var(&intDelay, "i", 0, "internal delay of atomic models (default 0)")
	flag.Float64Var(&extDelay, "e", 0, "external delay of atomic models (default 0)")
	flag.Float64Var(&prepTime, "p", 0, "preparation time of atomic models (default 0)")
	flag.Parse()

	if topology == "" {
		printHelpAndExit()
	}

	fmt.Printf("name: %v, topology: %v, depth: %v, width: %v, intDelay: %v, extDelay: %v, prepTime: %v\n",
		name, topology, depth, width, intDelay, extDelay, prepTime)

	start := time.Now()
	d, err := devstone.NewDEVStone(name, devstone.Topology(topology), depth, width, intDelay, extDelay, prepTime)
	if err != nil {
		fmt.Printf("Error: %v\n", err)
		printHelpAndExit()
	}
	elapsedModel := time.Since(start)
	fmt.Printf("Model creation time: %v\n", elapsedModel)
	model, ok := d.(modeling.Coupled)
	if !ok {
		panic("top-most DEVStone component does not comply with the modeling.Coupled interface")
	}
	coordinator := simulation.NewRootCoordinator(0, model)
	coordinator.Initialize()
	coordinator.SimInject(0, model.GetInPort("iIn"), []int{0})
	elapsedEngine := time.Since(start) - elapsedModel
	fmt.Printf("Engine setup time: %v\n", elapsedEngine)

	coordinator.SimulateTime(util.INFINITY)
	elapsedSimulation := time.Since(start) - elapsedEngine
	coordinator.Exit()
	fmt.Printf("Simulation time: %v\n", elapsedSimulation)
}
