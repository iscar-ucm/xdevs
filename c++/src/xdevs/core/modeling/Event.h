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

#ifndef SRC_XDEVS_CORE_MODELING_EVENT_H_
#define SRC_XDEVS_CORE_MODELING_EVENT_H_

#include <memory>

class Event {
protected:
	std::shared_ptr<void> sharedPtr;
public:
	Event();
	Event(const Event& src);
	const Event& operator=(const Event& right);
	virtual ~Event();

	void* getPtr();

	template <typename T> static Event makeEvent(T* ptr) {
		Event event;
		event.sharedPtr.reset();
		event.sharedPtr = std::shared_ptr<T>(ptr);
		return event;
	}
};

#endif /* SRC_XDEVS_CORE_MODELING_EVENT_H_ */
