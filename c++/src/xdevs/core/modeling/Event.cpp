/*
 * Event.cpp
 *
 *  Created on: 9 de may. de 2016
 *      Author: jlrisco
 */

#include "Event.h"

Event::Event() : sharedPtr(new int(0)), vtype(typeid(int).name()) {
}

Event::Event(const Event& src): sharedPtr(src.sharedPtr), vtype(src.vtype) {
}

const Event& Event::operator=(const Event& right) {
	sharedPtr.reset();
	sharedPtr = right.sharedPtr;
	vtype = right.vtype;
	return *this;
}

Event::~Event() {
	sharedPtr.reset();
}

void* Event::getPtr() {
	return sharedPtr.get();
}

std::shared_ptr<void> Event::getSharedPtr() {
	return sharedPtr;
}

const char* Event::getType() {
    return vtype;
}



