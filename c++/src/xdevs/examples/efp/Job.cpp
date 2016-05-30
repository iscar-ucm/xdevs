/*
 * Job.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Job.h"

Job::Job(const std::string& id) : id(id) , time(0) { }

Job::Job(const Job& src) : id(src.id), time(src.time) {}

const Job& Job::operator=(const Job& right) {
	this->id = right.id;
	this->time = right.time;
	return *(this);
}


Job::~Job() { }

void Job::setTime(const double& time) {
	this->time = time;
}

const double& Job::getTime() const { return time; }
const std::string& Job::getId() const { return id; }
