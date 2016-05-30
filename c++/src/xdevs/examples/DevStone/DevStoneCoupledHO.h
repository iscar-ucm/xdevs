#ifndef _DevStonecoupledHO_h_
#define _DevStonecoupledHO_h_

#include <string>
#include <stdio.h>
#include <stdlib.h>
#include "../../core/modeling/Port.h"
#include "../../core/modeling/Coupled.h"
#include "DevStoneAtomic.h"

class DevStoneCoupledHO : public Coupled {
public:
	/// Model input and output ports
	Port iIn, iInAux;
	Port oOut, oOutAux;

	DevStoneCoupledHO(const std::string& prefix, int width, int depth, double preparationTime, double intDelayTime, double extDelayTime);
	virtual ~DevStoneCoupledHO();
};

#endif
