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

#ifndef SRC_XDEVS_CORE_MODELING_ATOMIC_H_
#define SRC_XDEVS_CORE_MODELING_ATOMIC_H_

#include <string>
#include "Component.h"
#include "../util/Constants.h"

class Atomic : public Component {
protected:
	std::string phase;
	double sigma;
public:
	Atomic(const std::string& name);
	virtual ~Atomic();

	const double& ta();
    virtual void deltint() = 0;
    virtual void deltext(double e) = 0;
	void deltcon(double e);
	virtual void lambda() = 0;

	void holdIn(const std::string& phase, const double& sigma);
	void activate();
	void passivate();
	void passivateIn(const std::string& phase);

	bool phaseIs(const std::string& phase) const;
	const std::string& getPhase() const;
	void setPhase(const std::string& phase);
	const double& getSigma() const;
	void setSigma(const double& sigma);

	//std::string showState();

};

#endif /* SRC_XDEVS_CORE_MODELING_ATOMIC_H_ */
