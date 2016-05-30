/*
 * Coupled.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Coupled.h"

Coupled::Coupled(const std::string& name) : Component(name), components(), ic(), eic(), eoc() {
}

Coupled::~Coupled() {
	for(auto coupling : eic) {
		delete coupling;
	}
	for(auto coupling : eoc) {
		delete coupling;
	}
	for(auto coupling : ic) {
		delete coupling;
	}
}

void Coupled::initialize() {}
void Coupled::exit() {}

void Coupled::addCoupling(Component* cFrom, Port* pFrom, Component* cTo, Port* pTo) {
	Coupling* coupling = new Coupling(pFrom, pTo);
	// Add to connections
	if (cFrom == this) {
		eic.push_back(coupling);
	} else if (cTo == this) {
		eoc.push_back(coupling);
	} else {
		ic.push_back(coupling);
	}
}

void Coupled::addComponent(Component* component) {
	components.push_back(component);
}

const std::list<Component*>& Coupled::getComponents() const {
	return components;
}

const std::list<Coupling*>& Coupled::getIC() const { return ic; }
const std::list<Coupling*>& Coupled::getEIC() const { return eic; }
const std::list<Coupling*>& Coupled::getEOC() const { return eoc; }
