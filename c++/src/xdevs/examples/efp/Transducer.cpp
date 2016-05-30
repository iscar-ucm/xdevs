/*
 * Transducer.cpp
 *
 *  Created on: 8 de may. de 2016
 *      Author: jlrisco
 */

#include "Transducer.h"

Transducer::Transducer(const std::string& name, double observationTime) : Atomic(name), jobsArrived(), jobsSolved(), observationTime(observationTime), totalTa(0), clock(0), iArrived("arrived"), iSolved("solved"), oOut("out") {
	Component::addInPort(&iArrived);
	Component::addInPort(&iSolved);
	Component::addOutPort(&oOut);
}

Transducer::~Transducer() {
}

void Transducer::initialize() {
	Atomic::holdIn("active", observationTime);
}

void Transducer::exit() {
}

void Transducer::deltint() {
	clock += Atomic::getSigma();
	double throughput;
	double avgTaTime;
	if (Atomic::phaseIs("active")) {
		if (!jobsSolved.empty()) {
			avgTaTime = totalTa / jobsSolved.size();
			if (clock > 0.0) {
				throughput = jobsSolved.size() / clock;
			} else {
				throughput = 0.0;
			}
		} else {
			avgTaTime = 0.0;
			throughput = 0.0;
		}
		std::cout << "End time: " << clock << std::endl;
		std::cout << "Jobs arrived : " << jobsArrived.size() << std::endl;
		std::cout << "Jobs solved : " << jobsSolved.size() << std::endl;
		std::cout << "Average TA = " << avgTaTime << std::endl;
		std::cout << "Throughput = " << throughput << std::endl;
		Atomic::holdIn("done", 0);
	} else {
		Atomic::passivate();
	}
}

void Transducer::deltext(double e) {
	clock += e;
	if (Atomic::phaseIs("active")) {
		if (!iArrived.isEmpty()) {
			Event event = iArrived.getSingleValue();
			Job job(*(Job*)event.getPtr());
			std::cout << "Start job " << job.getId() << " @ t = " << clock << std::endl;
			job.setTime(clock);
			jobsArrived.push_back(job);
		}
		if (!iSolved.isEmpty()) {
			Event event = iSolved.getSingleValue();
			Job job(*(Job*)event.getPtr());
			totalTa += (clock - job.getTime());
			std::cout << "Finish jobAux " << job.getId() << " @ t = " << clock << std::endl;
			job.setTime(clock);
			jobsSolved.push_back(job);
		}
	}
	//logger.info("###Deltext: "+showState());
}

void Transducer::lambda() {
	if (Atomic::phaseIs("done")) {
		Event event;
		oOut.addValue(event);
	}
}
