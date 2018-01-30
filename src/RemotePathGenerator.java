import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

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
		NetworkTable.setIPAddress("127.0.0.1"); //NT server address - will be RoboRIO's.
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
			Waypoint[][] waypoints2d = deserializeWaypointArray2d((String)recievedObject);
			double timeStep = pathfinderInputTable.getNumber("timeStep", 0.05);
			double maxVel = pathfinderInputTable.getNumber("maxVel", 10);
			double maxAccel  = pathfinderInputTable.getNumber("maxAccel", 20);
			double maxJerk = pathfinderInputTable.getNumber("maxJerk", 30);
			
			Trajectory[] trajectories = generatePaths(waypoints2d, timeStep, maxVel, maxAccel, maxJerk);

			//output results to NT
			//genID is used for verification that the data is new, since if the same path object is output then valueChanged() doesn't fire.
			pathfinderOutputTable.putValue("path", serializeTrajectoryArray(trajectories));
			pathfinderOutputTable.putValue("ID", genID);
			System.out.println("client sent data and genID "+genID);
			genID++;
		}

		display.setBackground(Color.GREEN);
	}

	public Trajectory[] generatePaths(Waypoint[][] waypoints2d, double timeStep, double maxVel, double maxAccel, double maxJerk) {
		Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH,
				timeStep, maxVel, maxAccel, maxJerk);
		
		//first dimension of waypoints2d is what will be a trajectory, the second dimension is the waypoints in that trajectory
		Trajectory[] trajectories = new Trajectory[waypoints2d.length];
		for(int i=0;i<trajectories.length;i++) {
			try {
				Waypoint[] waypoints = waypoints2d[i];
				trajectories[i] = Pathfinder.generate(waypoints, config);
			} catch (Exception e) {
				display.setBackground(Color.BLACK); 
				display.setTitle(e.getMessage());
			}
		}
		
		return trajectories;
	}

	public String serializeTrajectoryArray(Trajectory[] trajectoryArray) {
		String serializedTrajectoryArray = ""; 
		
		try {
		     ByteArrayOutputStream bo = new ByteArrayOutputStream();
		     ObjectOutputStream so = new ObjectOutputStream(bo);
		     so.writeObject(trajectoryArray);
		     so.flush();
		     serializedTrajectoryArray = new String(Base64.getEncoder().encode(bo.toByteArray()));
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return serializedTrajectoryArray;
	}

	public Waypoint[][] deserializeWaypointArray2d(String serializedWaypointsArray2d) {
		Waypoint[][] waypoints2d = null; 
		try {
		     byte[] b = Base64.getDecoder().decode(serializedWaypointsArray2d.getBytes()); 
		     ByteArrayInputStream bi = new ByteArrayInputStream(b);
		     ObjectInputStream si = new ObjectInputStream(bi);
		     waypoints2d = (Waypoint[][]) si.readObject();
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return waypoints2d;
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
