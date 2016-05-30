/*
 * Simulator.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Simulator.h"

Simulator::Simulator(SimulationClock* clock, Atomic* model) : AbstractSimulator(clock), model(model) { }

Simulator::~Simulator() { }

void Simulator::initialize() {
	model->initialize();
	tL = clock->getTime();
	tN = tL + model->ta();
}

void Simulator::exit() {
	model->exit();
}

double Simulator::ta() {
	return model->ta();
}

void Simulator::deltfcn() {
	double t = clock->getTime();
	bool isInputEmpty = model->isInputEmpty();
	if (isInputEmpty && t != tN) {
		return;
	} else if (!isInputEmpty && t == tN) {
		double e = t - tL;
		model->setSigma(model->getSigma() - e);
		model->deltcon(e);
	} else if (isInputEmpty && t == tN) {
		model->deltint();
	} else if (!isInputEmpty && t != tN) {
		double e = t - tL;
		model->setSigma(model->getSigma() - e);
		model->deltext(e);
	}
	tL = t;
	tN = tL + model->ta();
}

void Simulator::lambda() {
	if (clock->getTime() == tN) {
		model->lambda();
	}
}

void Simulator::clear() {
	std::list<Port*> inPorts;
	inPorts = model->getInPorts();
	for (Port* port : inPorts) {
		port->clear();
	}
	std::list<Port*> outPorts;
	outPorts = model->getOutPorts();
	for (Port* port : outPorts) {
		port->clear();
	}
}

Atomic* Simulator::getModel() {
	return model;
}
