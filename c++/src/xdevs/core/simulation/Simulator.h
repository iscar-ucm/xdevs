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

#ifndef SRC_XDEVS_CORE_SIMULATION_SIMULATOR_H_
#define SRC_XDEVS_CORE_SIMULATION_SIMULATOR_H_

#include <list>
#include "../modeling/Atomic.h"
#include "AbstractSimulator.h"

class Simulator : public AbstractSimulator {
protected:
	Atomic* model;
public:
	Simulator(SimulationClock* clock, Atomic* model);
	virtual ~Simulator();
	virtual void initialize();
	virtual void exit();
	virtual double ta();
	virtual void deltfcn();
	virtual void lambda();
	virtual void clear();
	virtual Atomic* getModel();
};

#endif /* SRC_XDEVS_CORE_SIMULATION_SIMULATOR_H_ */
