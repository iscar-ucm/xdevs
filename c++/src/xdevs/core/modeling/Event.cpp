/*
 * Event.cpp
 *
 *  Created on: 9 de may. de 2016
 *      Author: jlrisco
 */

#include "Event.h"

Event::Event() : sharedPtr(new int(0)) {
}

Event::Event(const Event& src) {
	sharedPtr = src.sharedPtr;
}

const Event& Event::operator=(const Event& right) {
	sharedPtr.reset();
	sharedPtr = right.sharedPtr;
	return *(this);
}

Event::~Event() {
	sharedPtr.reset();
}

void* Event::getPtr() {
	return sharedPtr.get();
}




