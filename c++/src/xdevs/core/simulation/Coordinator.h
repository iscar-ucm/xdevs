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

#ifndef SRC_XDEVS_CORE_SIMULATION_COORDINATOR_H_
#define SRC_XDEVS_CORE_SIMULATION_COORDINATOR_H_

#include "../modeling/Coupled.h"
#include "../modeling/Atomic.h"
#include "../util/Constants.h"
#include "AbstractSimulator.h"
#include "Simulator.h"

class Coordinator : public AbstractSimulator {
protected:
	Coupled* model;
	std::list<AbstractSimulator*> simulators;
public:
	Coordinator(SimulationClock* clock, Coupled* model);
	Coordinator(Coupled* model);
	virtual ~Coordinator();

	virtual void initialize();
    virtual void exit();
    virtual std::list<AbstractSimulator*>& getSimulators();
    virtual double ta();
    virtual void lambda();
    virtual void propagateOutput();
    virtual void deltfcn();
    virtual void propagateInput();
    virtual void clear();
	virtual void simulate(long numIterations);
	virtual void simulate(double timeInterval);
	virtual Coupled* getModel();
};

#endif /* SRC_XDEVS_CORE_SIMULATION_COORDINATOR_H_ */
