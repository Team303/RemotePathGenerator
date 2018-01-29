
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
		
		//while(!Thread.interrupted()) {
			
		pathfinderInputTable.putString("waypoints", serializeWaypointArray(new Waypoint[] {
				new Waypoint(0, 0, 0),
				new Waypoint(1, 0, 0),
				new Waypoint(9, 0, 0),
				new Waypoint(10, 0, 0)
		}));

		NetworkTable.flush();
	
		//}
		
	}

	public static Trajectory deserializeTrajectory(String serializedTrajectory) {
		Trajectory trajectory = null; 
		try {
		     byte[] b = serializedTrajectory.getBytes(); 
		     ByteArrayInputStream bi = new ByteArrayInputStream(b);
		     ObjectInputStream si = new ObjectInputStream(bi);
		     trajectory = (Trajectory) si.readObject();
		 } catch (Exception e) {
		     e.printStackTrace();
		 }
		return trajectory;
	}
	
	public static String serializeWaypointArray(Waypoint[] waypoints) {
		 String serializedWaypoints = "";
		 try {
		     ByteArrayOutputStream bo = new ByteArrayOutputStream();
		     ObjectOutputStream so = new ObjectOutputStream(bo);
		     so.writeObject(waypoints);
		     so.flush();
		     serializedWaypoints = bo.toString();
		 } catch (Exception e) {
		     e.printStackTrace();
		 }
		return serializedWaypoints;
	}
	
}
