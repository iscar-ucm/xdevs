#ifndef __DevStoneAtomic_h_
#define __DevStoneAtomic_h_

#include <cstdlib>
#include <list>
#include <string>
#include "../../core/modeling/Port.h"
#include "../../core/modeling/Atomic.h"
#include "Dhrystone.h"


class DevStoneAtomic: public Atomic {
protected:	
    double preparationTime;
    double intDelayTime;
    double extDelayTime;
    std::list<long> outValues;
    Dhrystone dhrystone;
public:
	/// Model input port
    Port iIn;
    Port oOut;
	static long NUM_DELT_INTS;
	static long NUM_DELT_EXTS;
	static long NUM_EVENT_EXTS;

	/// Constructor.
	DevStoneAtomic(const std::string& name, double preparationTime, double intDelayTime, double extDelayTime);
	virtual ~DevStoneAtomic();

	virtual void initialize();
	virtual void exit();
	virtual void deltint();
	virtual void deltext(double e);
	virtual void lambda();
};

#endif
