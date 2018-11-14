/*
 * Generator.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Generator.h"

Generator::Generator(const std::string& name, const double& period) : Atomic(name), jobCounter(1), period(period), oOut("out") {
	this->addOutPort(&oOut);
}

Generator::~Generator() {
}

void Generator::initialize() {
	jobCounter = 1;
	this->holdIn("active", period);
}

void Generator::exit() {
}

void Generator::deltint() {
	jobCounter++;
	this->holdIn("active", period);
}

void Generator::deltext(double e) {
	this->passivate();
}

void Generator::lambda() {
    float *arr = new float[5];
    for(int i=0; i<5; i++) arr[i] = jobCounter + i;
	Event event = Event::makeEvent<float>(arr);
	oOut.addValue(event);
}
