
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

public class NetworkTablesTester {

	public static final double timeStep = 0.05;
	public static final double maxVel = 10;
	public static final double maxAccel = 8;
	public static final double maxJerk = 60;

	/**
	 * This will actually be the roboRio
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		NetworkTable.setServerMode();
		NetworkTable.setTeam(303);
		NetworkTable.initialize();

		NetworkTable pathfinderInputTable = NetworkTable.getTable("pathfinderInput");
		NetworkTable pathfinderOutputTable = NetworkTable.getTable("pathfinderOutput");		

		pathfinderOutputTable.addTableListener(new ITableListener() {
			@Override
			public void valueChanged(ITable table, String string, Object recievedObject, boolean newValue) {
				System.out.println("[server] String: "+string+" Value: "+recievedObject+" new: "+newValue);
			}
		});

		pathfinderInputTable.putNumber("timeStep", timeStep);
		pathfinderInputTable.putNumber("maxVel", maxVel);
		pathfinderInputTable.putNumber("maxAccel", maxAccel);
		pathfinderInputTable.putNumber("maxJerk", maxJerk);

		pathfinderInputTable.putString("waypoints", serializeWaypointArray2d(new Waypoint[][] {
			{//this is waypoints[0], and will output to trajectories[0]
				new Waypoint(0, 0, 0), 
				new Waypoint(0, 0, 0), //this point is waypoints[0, 1]
				new Waypoint(0, 0, 0)
			}, {
				new Waypoint(0, 0, 0), //this point is waypoints[1, 0]
				new Waypoint(0, 0, 0)
			}, {//this is waypoints[2] and will output to trajectories[2]
				new Waypoint(0, 0, 0),
				new Waypoint(0, 0, 0),
				new Waypoint(0, 0, 0)
			}
		}));

		NetworkTable.flush();

		while (!Thread.interrupted()) {
			Thread.sleep(100);
		}

	}

	public static Trajectory[] deserializeTrajectoryArray(String serializedTrajectoryArray) {
		Trajectory[] trajectories = null; 
		try {
			byte[] b = Base64.getDecoder().decode(serializedTrajectoryArray.getBytes()); 
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			trajectories = (Trajectory[]) si.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trajectories;
	}

	public static String serializeWaypointArray2d(Waypoint[][] waypoints2d) {
		String serializedWaypoints = "";
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(bo);
			so.writeObject(waypoints2d);
			so.flush();
			serializedWaypoints = new String(Base64.getEncoder().encode(bo.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serializedWaypoints;
	}

}
