/*
 * SimulationClock.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "SimulationClock.h"

SimulationClock::SimulationClock() : time(0) { }

SimulationClock::SimulationClock(const double& time) : time(time) { }

SimulationClock::~SimulationClock() { }

double SimulationClock::getTime() { return time; }

void SimulationClock::setTime(const double& time) { this->time = time; }
