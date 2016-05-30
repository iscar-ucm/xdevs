#ifndef _DevStoneGenerator_h_
#define _DevStoneGenerator_h_

#include <string>
#include "../../core/modeling/Event.h"
#include "../../core/modeling/Port.h"
#include "../../core/modeling/Atomic.h"

class DevStoneGenerator: public Atomic {
protected:
	double preparationTime;
	double period;
	long maxEvents;
	long counter;
public:
	Port oOut;

	DevStoneGenerator(const std::string& name, double preparationTime, double period, long maxEvents);
	virtual ~DevStoneGenerator();

	virtual void initialize();
	virtual void exit();
	virtual void deltint();
	virtual void deltext(double e);
	virtual void lambda();

};

#endif
