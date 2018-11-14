
#include "Logger.h"

Logger::Logger(const std::string& name) : Atomic(name), nextEvent(), iIn("in") {
	this->addInPort(&iIn);
}

Logger::~Logger() {
}

void Logger::initialize() {
	Atomic::passivate();
}

void Logger::exit() {
}

void Logger::deltint() {
	Atomic::passivate();
}

void Logger::deltext(double e) {
    Event event;

	for(std::list<Event>::const_iterator it = iIn.getValues().begin(); it != iIn.getValues().end(); it++) {
        event = *it;
        std::cout << std::to_string(*(float*)(event.getPtr())) << std::endl;
	}
}

void Logger::lambda() {
}
