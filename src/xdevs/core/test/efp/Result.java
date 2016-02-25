/*
 * Copyright (C) 2014-2015 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>.
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
package xdevs.core.test.efp;

import xdevs.core.modeling.Entity;

/**
 * @author smittal
 *
 */

public class Result extends Entity{

	double thruput, avgTaTime;
	int arrived, solved;
	
	public Result(){
		super("Result");
	}
	
	public Result(double throughput, double avgTaTime, int arrived, int solved){
		this.thruput = throughput;
		this.avgTaTime = avgTaTime;
		this.arrived = arrived;
		this.solved = solved;
	}
}
