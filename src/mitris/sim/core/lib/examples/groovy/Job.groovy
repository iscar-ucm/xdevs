package mitris.sim.core.lib.examples.groovy

import mitris.sim.core.modeling.api.Entity

class Job implements Entity{

	protected String id
	protected double time
	
	Job(String name){
		id = name
		time = 0.0
	}

	
	@Override
	public String getName() {
		return id
	}
	
	String toString(){
		return id
	}

}
