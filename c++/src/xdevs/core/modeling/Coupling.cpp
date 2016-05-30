/*
 * Coupling.cpp
 *
 *  Created on: 6 de may. de 2016
 *      Author: jlrisco
 */

#include "Coupling.h"

Coupling::Coupling(Port* portFrom, Port* portTo) {
	this->portFrom = portFrom;
	this->portTo = portTo;
}

Coupling::~Coupling() {}

// Coupling members
void Coupling::propagateValues() { portTo->addValues(portFrom->getValues()); }
const Port* Coupling::getPortFrom() const { return portFrom; }
const Port* Coupling::getPortTo() const { return portTo; }

/*std::string Coupling::toString() {
	std::string ret;
	ret << "(" << portFrom->getName() << "->" << portTo->getName() << ")";
	return ret;
}*/
