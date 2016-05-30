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

#ifndef SRC_XDEVS_EXAMPLES_EFP_JOB_H_
#define SRC_XDEVS_EXAMPLES_EFP_JOB_H_

#include <string>

class Job {
protected:
	std::string id;
	double time;
public:
	Job(const std::string& id);
	Job(const Job& src);
	const Job& operator=(const Job& right);
	virtual ~Job();

	void setTime(const double& time);
	const double& getTime() const;
	const std::string& getId() const;
};

#endif /* SRC_XDEVS_CORE_TEST_EFP_JOB_H_ */
