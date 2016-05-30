/*
 * Atomic.cpp
 *
 *  Created on: 5 de may. de 2016
 *      Author: jlrisco
 */

#include "Atomic.h"

Atomic::Atomic(const std::string& name) : Component(name), phase(Constants::PHASE_PASSIVE)
, sigma(Constants::INFINITY) {
}


Atomic::~Atomic() {
	// TODO Auto-generated destructor stub
}

const double& Atomic::ta() {
	return sigma;
}

void Atomic::deltcon(double e) {
	deltint();
	deltext(0);
}

void Atomic::holdIn(const std::string& phase, const double& sigma) {
	this->phase = phase;
	this->sigma = sigma;
}

void Atomic::activate() {
	this->phase = Constants::PHASE_ACTIVE;
	this->sigma = 0;
}

void Atomic::passivate() {
	this->phase = Constants::PHASE_PASSIVE;
	this->sigma = Constants::INFINITY;
}

void Atomic::passivateIn(const std::string& phase) {
	this->phase = phase;
	this->sigma = Constants::INFINITY;
}

bool Atomic::phaseIs(const std::string& phase) const {
	return this->phase == phase;
}

const std::string& Atomic::getPhase() const {
	return phase;
}

void Atomic::setPhase(const std::string& phase) {
	this->phase = phase;
}

const double& Atomic::getSigma() const {
	return this->sigma;
}

void Atomic::setSigma(const double& sigma) {
	this->sigma = sigma;
}

/*std::string Atomic::showState() {
	std::string sb;
	sb << name;
	sb << ":[";
	sb << "\tstate: " << phase;
	sb << "\t, sigma: " << sigma;
	sb << "]";
	return sb;
}*/
