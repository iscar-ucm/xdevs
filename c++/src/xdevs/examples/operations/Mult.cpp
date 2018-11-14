/*
 * Mult.cpp
 *
 *  Created on: 8 de may. de 2016
 *      Author: jlrisco
 */

#include "Mult.h"

Mult::Mult(const std::string& name, int numToMult) : Atomic(name), nextEvent(), numToMult(numToMult), iIn("in"), oOut("out") {
	this->addInPort(&iIn);
	this->addOutPort(&oOut);
}

Mult::~Mult() {
}

void Mult::initialize() {
	Atomic::passivate();
}

void Mult::exit() {
}

void Mult::deltint() {
	Atomic::passivate();
}

void Mult::deltext(double e) {
    nextEvent = iIn.getSingleValue();
    nextEvent = Event::makeEvent(new float(*(float*)nextEvent.getPtr() * numToMult));
    Atomic::holdIn("active", 0);
}

void Mult::lambda() {
	oOut.addValue(nextEvent);
}
