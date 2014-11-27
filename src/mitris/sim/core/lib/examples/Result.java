/**
 * 
 */
package mitris.sim.core.lib.examples;

import lombok.Data;
import mitris.sim.core.modeling.EntityBase;

/**
 * @author smittal
 *
 */

@Data
public class Result extends EntityBase{

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
