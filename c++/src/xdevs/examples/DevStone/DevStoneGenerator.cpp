#include "DevStoneGenerator.h"


DevStoneGenerator::DevStoneGenerator(const std::string& name, double preparationTime, double period, long maxEvents)
	: Atomic(name),
	  preparationTime(preparationTime),
	  period(period),
	  maxEvents(maxEvents),
	  counter(0),
	  oOut("out") {
	Component::addOutPort(&oOut);
}

DevStoneGenerator::~DevStoneGenerator() { }

void DevStoneGenerator::initialize() {
	this->counter = 1;
	Atomic::holdIn("active", preparationTime);
}

void DevStoneGenerator::exit() { }

void DevStoneGenerator::deltint() {
	counter++;
	if (counter > maxEvents) {
		Atomic::passivate();
	} else {
		Atomic::holdIn("active", period);
	}
}

void DevStoneGenerator::deltext(double e) {
	Atomic::passivate();
}

void DevStoneGenerator::lambda() {
	Event event = Event::makeEvent(new long(counter));
	oOut.addValue(event);
}
