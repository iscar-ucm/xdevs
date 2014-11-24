package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.api.Entity

class Job implements Entity{
	var String id
	var double time
	
	new(String name){
		id = name
		time = 0.0
	}

	def setId(String id){
		this.id = id
	}
	
	def setTime(double time){
		this.time = time
	}
	
	def double getTime(){
	 	time
	}
	
	def String getId(){
		id
	}
	
	override getName() {
		return name
	}
	
	override toString(){
		"Job{"+id+","+time+"}"
	}
}