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

#include "Ef.h"

Ef::Ef(const std::string& name, const double& period, const double& observationTime)
			: Coupled(name),
			  generator("generator", period),
			  transducer("transducer", observationTime),
			  iIn("in"),
			  oOut("out") {
	Component::addInPort(&iIn);
	Component::addOutPort(&oOut);
	Coupled::addComponent(&generator);
	Coupled::addComponent(&transducer);
	Coupled::addCoupling(this, &(this->iIn), &transducer, &transducer.iSolved);
	Coupled::addCoupling(&generator, &generator.oOut, this, &(this->oOut));
	Coupled::addCoupling(&generator, &generator.oOut, &transducer, &transducer.iArrived);
	Coupled::addCoupling(&transducer, &transducer.oOut, &generator, &generator.iStop);
}

Ef::~Ef() { }

