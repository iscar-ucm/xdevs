
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
        float *arr = (float*) event.getPtr();
        //std::cout << "logger: " << arr[0] << "|||";
        std::cout << "[";
        for(int i=0; i<5; i++) std::cout << arr[i] << " ";
        std::cout << "]" << std::endl;
	}

	std::cout << std::endl;
}

void Logger::lambda() {
}
