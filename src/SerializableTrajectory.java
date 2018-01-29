import java.io.Serializable;

import jaci.pathfinder.Trajectory;

public class SerializableTrajectory extends Trajectory implements Serializable{

	public SerializableTrajectory(int length) {
		super(length);
	}

	public SerializableTrajectory(Segment[] segments) {
		super(segments);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4367985962021119268L;

}
