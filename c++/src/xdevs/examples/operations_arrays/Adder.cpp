/*
 * Adder.cpp
 *
 *  Created on: 8 de may. de 2016
 *      Author: jlrisco
 */

#include "Adder.h"

Adder::Adder(const std::string& name, int numToAdd) : Atomic(name), nextEvent(), numToAdd(numToAdd), iIn("in"), oOut("out") {
	this->addInPort(&iIn);
	this->addOutPort(&oOut);
}

Adder::~Adder() {
}

void Adder::initialize() {
	Atomic::passivate();
}

void Adder::exit() {
}

void Adder::deltint() {
	Atomic::passivate();
}

void Adder::deltext(double e) {
    nextEvent = iIn.getSingleValue();
    float *arr = (float *) nextEvent.getPtr();
    float *narr = new float[5];
    for(int i=0; i<5; i++) narr[i] = arr[i] + numToAdd;
    nextEvent = Event::makeEvent<float>(narr);
    Atomic::holdIn("active", 0);
}

void Adder::lambda() {
	oOut.addValue(nextEvent);
}
