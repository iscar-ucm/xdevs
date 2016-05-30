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

#ifndef SRC_XDEVS_CORE_MODELING_COUPLED_H_
#define SRC_XDEVS_CORE_MODELING_COUPLED_H_

#include <list>
#include <string>
#include "Component.h"
#include "Coupling.h"
#include "Port.h"

class Coupled : public Component {
protected:
    std::list<Component*> components;
    std::list<Coupling*> ic;
    std::list<Coupling*> eic;
    std::list<Coupling*> eoc;
public:
	Coupled(const std::string& name);
	virtual ~Coupled();

	virtual void initialize();
	virtual void exit();

	void addCoupling(Component* cFrom, Port* pFrom, Component* cTo, Port* pTo);
	void addComponent(Component* component);
    const std::list<Component*>& getComponents() const;
    const std::list<Coupling*>& getIC() const;
    const std::list<Coupling*>& getEIC() const;
    const std::list<Coupling*>& getEOC() const;
	// TODO: Implement this function
	// CoupledInterface flatten()
};

#endif /* SRC_XDEVS_CORE_MODELING_COUPLED_H_ */
