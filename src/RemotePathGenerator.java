import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.IRemote;
import edu.wpi.first.wpilibj.tables.IRemoteConnectionListener;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

public class RemotePathGenerator implements ITableListener, IRemoteConnectionListener {

	NetworkTable pathfinderInputTable;
	NetworkTable pathfinderOutputTable;
	boolean connected = false;
	int genID = 1;
	DisplayFrame display;
	
	public static void main(String[] args) {
		NetworkTable.setClientMode();
		NetworkTable.setTeam(303);
		NetworkTable.setIPAddress("10.3.3.2"); //NT server address - will be RoboRIO's.
		new RemotePathGenerator();
	}

	public RemotePathGenerator() {
		display = new DisplayFrame();
		pathfinderInputTable = NetworkTable.getTable("pathfinderInput");
		pathfinderOutputTable = NetworkTable.getTable("pathfinderOutput");
		pathfinderInputTable.addTableListener(this, true);
		NetworkTable.addGlobalConnectionListener(this, true);
	}

	@Override
	public void valueChanged(ITable iTable, String string, Object recievedObject, boolean newValue) {
		System.out.println("[client recieved] String: "+string+" Value: "+recievedObject+" new: "+newValue);

		if(string.equals("waypoints")) {
			display.setBackground(Color.YELLOW);
			HashMap<String, Waypoint[]> waypointArrayMap = deserializeWaypointArrayMap((String)recievedObject);
			double timeStep = pathfinderInputTable.getNumber("timeStep", 0.05);
			double maxVel = pathfinderInputTable.getNumber("maxVel", 10);
			double maxAccel  = pathfinderInputTable.getNumber("maxAccel", 20);
			double maxJerk = pathfinderInputTable.getNumber("maxJerk", 30);
			
			HashMap<String, Trajectory> trajectories = generatePaths(waypointArrayMap, timeStep, maxVel, maxAccel, maxJerk);

			//output results to NT
			//genID is used for verification that the data is new, since if the same path object is output then valueChanged() doesn't fire.
			//genID is probably not needed
			pathfinderOutputTable.putValue("path", serializeTrajectoryMap(trajectories));
			pathfinderOutputTable.putValue("ID", genID);
			System.out.println("client sent data and genID "+genID);
			genID++;
			
		//	for(int i = 0; i < trajectories; i++) {
		//		System.out.printf("%d) x: %.2f y: %.2f heading: %.2f velocity: %.2f acceleration: %.2f jerk: %.2f \n" + "", i, trajectories[0].segments[i].x, trajectories[0].segments[i].y, Pathfinder.r2d(trajectories[0].segments[i].heading), trajectories[0].segments[i].velocity, trajectories[0].segments[i].acceleration,  trajectories[0].segments[i].jerk);
		//	}
		}
		display.setBackground(Color.GREEN);
	}

	public HashMap<String, Trajectory> generatePaths(HashMap<String, Waypoint[]> waypointArrayMap, double timeStep, double maxVel, double maxAccel, double maxJerk) {
		Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH,
				timeStep, maxVel, maxAccel, maxJerk);
		
		HashMap<String, Trajectory> trajectories = new HashMap<String, Trajectory>(waypointArrayMap.size());
		
		Set<String> keys = waypointArrayMap.keySet();
		for(String key : keys) {
			try {
				trajectories.put(key, Pathfinder.generate(waypointArrayMap.get(key), config));
			} catch (Exception e) {
				display.setBackground(Color.BLACK);
				display.setTitle(e.getMessage());
			}
		}
		
		return trajectories;
	}

	public String serializeTrajectoryMap(HashMap<String, Trajectory> trajectoryMap) {
		String serializedTrajectoryMap = "";
		try {
		     ByteArrayOutputStream bo = new ByteArrayOutputStream();
		     ObjectOutputStream so = new ObjectOutputStream(bo);
		     so.writeObject(trajectoryMap);
		     so.flush();
		     serializedTrajectoryMap = new String(Base64.getEncoder().encode(bo.toByteArray()));
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return serializedTrajectoryMap;
	}
	
	public HashMap<String, Waypoint[]> deserializeWaypointArrayMap(String serializedWaypointArrayMap) {
		HashMap<String, Waypoint[]> waypointArrayMap = null; 
		try {
		     byte[] b = Base64.getDecoder().decode(serializedWaypointArrayMap.getBytes()); 
		     ByteArrayInputStream bi = new ByteArrayInputStream(b);
		     ObjectInputStream si = new ObjectInputStream(bi);
		     waypointArrayMap = (HashMap<String, Waypoint[]>) si.readObject();
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return waypointArrayMap;
	}
	
	@Override
	public void connected(IRemote arg0) {
		display.setBackground(Color.GREEN);
		connected = true;
	}

	@Override
	public void disconnected(IRemote arg0) {
		display.setBackground(Color.RED);
		connected = false;
	}

}
