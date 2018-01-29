import java.io.Serializable;

import jaci.pathfinder.Waypoint;

public class SerializableWaypoint extends Waypoint implements Serializable{

	public SerializableWaypoint(double x, double y, double angle) {
		super(x, y, angle);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7489441688830317167L;

}
