/*
 * Copyright (C) 2016-2016 José Luis Risco Martín <jlrisco@ucm.es>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * http://www.gnu.org/licenses/
 *
 * Contributors:
 *  - José Luis Risco Martín
 */

#ifndef SRC_XDEVS_CORE_SIMULATION_ABSTRACTSIMULATOR_H_
#define SRC_XDEVS_CORE_SIMULATION_ABSTRACTSIMULATOR_H_

#include "SimulationClock.h"
#include "../modeling/Component.h"

class AbstractSimulator {
protected:
	SimulationClock* clock;
	double tL; // Time of last event
	double tN; // Time of next event
public:
	AbstractSimulator(SimulationClock* clock);
	virtual ~AbstractSimulator();

	virtual void initialize() = 0;
	virtual void exit() = 0;
	virtual double ta() = 0;
	virtual void lambda() = 0;
	virtual void deltfcn() = 0;
	virtual void clear() = 0;
	virtual Component* getModel() = 0;
	double getTL();
	void setTL(double tL);
	double getTN();
	void setTN(double tN);
	SimulationClock* getClock();
};

#endif /* SRC_XDEVS_CORE_SIMULATION_ABSTRACTSIMULATOR_H_ */
