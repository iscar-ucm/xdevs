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

Ef::Ef(const std::string& name, const double& period)
			: Coupled(name),
			  generator("generator", period),
			  logger("logger"),
			  adder("adder", 10),
			  adder2("adder2", 20),
			  mult("mult", 3) {
	Coupled::addComponent(&generator);
	Coupled::addComponent(&logger);
	Coupled::addComponent(&adder);
	Coupled::addComponent(&adder2);
	Coupled::addComponent(&mult);

	Coupled::addCoupling(&generator, &generator.oOut, &adder, &adder.iIn);
	Coupled::addCoupling(&generator, &generator.oOut, &adder2, &adder2.iIn);
	Coupled::addCoupling(&generator, &generator.oOut, &mult, &mult.iIn);

	Coupled::addCoupling(&adder, &adder.oOut, &logger, &logger.iIn);
	Coupled::addCoupling(&adder2, &adder2.oOut, &logger, &logger.iIn);
	Coupled::addCoupling(&mult, &mult.oOut, &logger, &logger.iIn);
}

Ef::~Ef() { }

