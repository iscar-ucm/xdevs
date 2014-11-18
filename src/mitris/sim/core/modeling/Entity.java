package mitris.sim.core.modeling;

public class Entity {
	protected String name;

	public Entity (String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
