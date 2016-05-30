/*
 * Processor.cpp
 *
 *  Created on: 8 de may. de 2016
 *      Author: jlrisco
 */

#include "Processor.h"

Processor::Processor(const std::string& name, double processingTime) : Atomic(name), nextEvent(), processingTime(processingTime), iIn("in"), oOut("out") {
	this->addInPort(&iIn);
	this->addOutPort(&oOut);
}

Processor::~Processor() {
}

void Processor::initialize() {
	Atomic::passivate();
}

void Processor::exit() {
}

void Processor::deltint() {
	Atomic::passivate();
}

void Processor::deltext(double e) {
	if (Atomic::phaseIs("passive")) {
		nextEvent = iIn.getSingleValue();
		Atomic::holdIn("active", processingTime);
	}
}

void Processor::lambda() {
	oOut.addValue(nextEvent);
}
