/*
 * Component.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Component.h"

Component::Component(const std::string& name) : name(name), inPorts(), outPorts() {}

Component::~Component() {}

bool Component::isInputEmpty() {
	std::list<Port*>::iterator itr;
	for(itr = inPorts.begin(); itr != inPorts.end(); ++itr) {
		Port* port = *itr;
		if(!port->isEmpty()) {
			return false;
		}
	}
	return true;
}

void Component::addInPort(Port* port) {
	inPorts.push_back(port);
}

const std::list<Port*>& Component::getInPorts() {
	return inPorts;
}

const std::string Component::getName() {
	return name;
}

void Component::addOutPort(Port* port) {
	outPorts.push_back(port);
}

const std::list<Port*>& Component::getOutPorts() {
	return outPorts;
}

/*const std::string& Component::toString(){
	std::string sb;
	sb << name << " : Inports[ ";
	std::list<const Port*>::iterator itr;
	for(itr = inPorts.begin(); itr != inPorts.end(); ++itr) {
		Port* port = *itr;
		sb << port->getName() << " ";
	}
	sb << "] Outports[ ";
	for(itr = outPorts.end(); itr != outPorts.end(); ++itr) {
		Port* port = *itr;
		sb << port->getName() << " ";
	}
	sb << "]";
	return sb;
}*/
