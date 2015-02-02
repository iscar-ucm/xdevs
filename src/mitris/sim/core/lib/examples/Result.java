/**
 * 
 */
package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.Entity;

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
