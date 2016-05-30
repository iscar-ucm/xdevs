/*
 * AbstractSimulator.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "AbstractSimulator.h"

AbstractSimulator::AbstractSimulator(SimulationClock* clock) : clock(clock), tL(0), tN(0) {
}

AbstractSimulator::~AbstractSimulator() { }

double AbstractSimulator::getTL() {
	return tL;
}

void AbstractSimulator::setTL(double tL) {
	this->tL = tL;
}

double AbstractSimulator::getTN() {
	return tN;
}

void AbstractSimulator::setTN(double tN) {
	this->tN = tN;
}

SimulationClock* AbstractSimulator::getClock() {
	return clock;
}
