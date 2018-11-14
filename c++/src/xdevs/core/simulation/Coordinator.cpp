/*
 * Coordinator.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Coordinator.h"

Coordinator::Coordinator(SimulationClock* clock, Coupled* model, std::string rulesFilePath = "") : AbstractSimulator(clock) {
	this->model = model;
	// Build hierarchy
	std::list<Component*> components = model->getComponents();
	for (auto component : components) {
		// Coupled* newType = dynamic_cast<Coupled*>(component)
		if (dynamic_cast<Coupled*>(component)) {
			Coordinator* coordinator = new Coordinator(clock, (Coupled*)component);
			simulators.push_back(coordinator);
			// Atomic* newType = dynamic_cast<Atomic*>(component)
		} else if (dynamic_cast<Atomic*>(component)) {
			Simulator* simulator = new Simulator(clock, (Atomic*)component);
			simulators.push_back(simulator);
		}
	}

	if(rulesFilePath != "") rulesEval.parseRules(rulesFilePath, model);
}

Coordinator::Coordinator(Coupled* model) : Coordinator(new SimulationClock(), model) {}

Coordinator::Coordinator(Coupled* model, std::string rulesFilePath) : Coordinator(new SimulationClock(), model, rulesFilePath) {}

Coordinator::~Coordinator() {
	for(auto simulator : simulators) {
		delete simulator;
	}
}

void Coordinator::initialize() {
	for (auto simulator : simulators) {
		simulator->initialize();
	}
	tL = clock->getTime();
	tN = tL + this->ta();
}

void Coordinator::exit() {
	for (auto simulator : simulators) {
		simulator->exit();
	}
}

std::list<AbstractSimulator*>& Coordinator::getSimulators() {
	return simulators;
}

void Coordinator::lambda() {
	for (auto simulator : simulators) {
		simulator->lambda();
	}
	propagateOutput();
}

double Coordinator::ta() {
	double tn = std::numeric_limits<double>::infinity();
	for (auto simulator : simulators) {
		if (simulator->getTN() < tn) {
			tn = simulator->getTN();
		}
	}
	return tn - clock->getTime();
}

void Coordinator::propagateOutput() {
	auto ic = model->getIC();
	for (auto c : ic) {
		c->propagateValues();
	}

	auto eoc = model->getEOC();
	for (auto c : eoc) {
		c->propagateValues();
	}
}

void Coordinator::deltfcn() {
	propagateInput();
	for (auto simulator : simulators) {
		simulator->deltfcn();
	}
	tL = clock->getTime();
	tN = tL + this->ta();
}

void Coordinator::propagateInput() {
	auto eic = model->getEIC();
	for (auto c : eic) {
		c->propagateValues();
	}
}

void Coordinator::clear() {
	for (auto simulator : simulators) {
		simulator->clear();
	}
	auto inPorts = model->getInPorts();
	for (auto port : inPorts) {
		port->clear();
	}
	auto outPorts = model->getOutPorts();
	for (auto port : outPorts) {
		port->clear();
	}
}

void Coordinator::simulate(long numIterations) {
	clock->setTime(tN);
	long counter;
	for (counter = 1; counter < numIterations && clock->getTime() < std::numeric_limits<double>::infinity(); counter++) {
		this->lambda();
		this->deltfcn();
		if(rulesEval.hasRules()) rulesEval.checkRules();
		this->clear();
		clock->setTime(tN);
	}
}

void Coordinator::simulate(double timeInterval) {
	clock->setTime(tN);
	double tF = clock->getTime() + timeInterval;
	while (clock->getTime() < std::numeric_limits<double>::infinity() && clock->getTime() < tF) {
		this->lambda();
		this->deltfcn();
		this->clear();
		clock->setTime(tN);
	}
}

Coupled* Coordinator::getModel() {
	return model;
}
