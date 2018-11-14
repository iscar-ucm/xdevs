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

Component* Coupled::compFromPath(std::string path) {
    //std::cout << "compFromPath: " << path << std::endl;
    Component* currComp = this;
    Component* nextComp;

    size_t last = 0, next = 0;
    std::string term;
    while ((next = path.find(".", last)) != std::string::npos) {
        term = path.substr(last, next-last);
        nextComp = nullptr;

        if(dynamic_cast<Coupled*>(currComp) == nullptr) return nullptr;

        for(Component* comp: ((Coupled*) currComp)->getComponents()) {
            if(comp->getName() == term) {
                nextComp = comp;
                break;
            }
        }

        if(nextComp == nullptr) return nullptr;
        currComp = nextComp;
        last = next;
    }

    if(dynamic_cast<Coupled*>(currComp) == nullptr) return nullptr;

    for(Component* comp: ((Coupled*) currComp)->getComponents()) {
        term = path.substr(last);
        if(comp->getName() == term) {
            //std::cout << "found: " << path << std::endl;
            return comp;
        }
    }
    return nullptr;
}

Port* Coupled::portFromPath(std::string path) {
    //std::cout << "portFromPath: " << path << std::endl;
    size_t pos = path.rfind(".");

    if(pos == std::string::npos)
        return nullptr;

    std::string compPath = path.substr(0, pos);
    std::string portName = path.substr(pos+1);

    Component* comp = compFromPath(compPath);

    if(comp != nullptr) {
        for(Port* port: comp->getInPorts()) {
            if(port->getName() == portName) {
            std::cout << "port found: " << port->getName() << std::endl;
                return port;
            }
        }

        for(Port* port: comp->getOutPorts()) {
            if(port->getName() == portName) {
                std::cout << "port found: " << port->getName() << std::endl;
                return port;
            }
        }
    }

    return nullptr;
}
