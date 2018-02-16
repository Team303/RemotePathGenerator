
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
/**
 * Class designed to test construction of Trajectories 
 */
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

		HashMap<String, Waypoint[]> waypointArrayMap = new HashMap<>(); 

		waypointArrayMap.put("this is where the path name goes", new Waypoint[] {
				new Waypoint(0, 0, 0),
				new Waypoint(1, 0, 0),
				new Waypoint(3, 0, 0)
		});

		waypointArrayMap.put("insert another name here", new Waypoint[] {
				new Waypoint(0, 0, 0),
				new Waypoint(1, 0, 0),
				new Waypoint(2, 0, 0)
		});

		pathfinderInputTable.putString("waypoints", serializeWaypointArrayMap(waypointArrayMap));

		NetworkTable.flush();

		while (!Thread.interrupted()) {
			Thread.sleep(100);
		}

	}

	public static HashMap<String, Trajectory> deserializeTrajectoryMap(String serializedTrajectoryMap) {
		HashMap<String, Trajectory> trajectories = null; 
		try {
			byte[] b = Base64.getDecoder().decode(serializedTrajectoryMap.getBytes()); 
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			trajectories = (HashMap<String, Trajectory>) si.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trajectories;
	}

	public static String serializeWaypointArrayMap(HashMap<String, Waypoint[]> waypointArrayMap) {
		String serializedWaypointMap = "";
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(bo);
			so.writeObject(waypointArrayMap);
			so.flush();
			serializedWaypointMap = new String(Base64.getEncoder().encode(bo.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serializedWaypointMap;
	}

}
