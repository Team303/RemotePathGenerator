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

public class RemotePathGenerator extends JFrame implements ITableListener, IRemoteConnectionListener {

	private static final long serialVersionUID = -3371908382388429620L;
	NetworkTable pathfinderInputTable;
	NetworkTable pathfinderOutputTable;
	boolean connected = false;
	int genID = 1;

	/*static {
		System.loadLibrary("pathfinderjava.dll");
	}*/
	static {
	  //  try {
	    	System.load("C:\\Users\\Bradley\\workspace\\RemotePathGenerator\\pathfinderjava.dll");
//	    } catch (UnsatisfiedLinkError e) {
//	      System.err.println("Native code library failed to load.\n" + e);
//	      System.exit(1);
//	    }
	  }
	
	public static void main(String[] args) {
		NetworkTable.setClientMode();
		NetworkTable.setTeam(303);
		NetworkTable.setIPAddress("192.168.43.158"); //NT server address - will be RoboRIO's. TODO
		new RemotePathGenerator();
	}

	public RemotePathGenerator() {
		this.setTitle("Remote Path Generator Status");
		this.setSize(450, 300);
		this.getContentPane().setBackground(Color.RED);
		this.setAlwaysOnTop(true);
		this.setUndecorated(true);
		this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		//this.setOpacity(0.5f);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

		pathfinderInputTable = NetworkTable.getTable("pathfinderInput");
		pathfinderOutputTable = NetworkTable.getTable("pathfinderOutput");
		pathfinderInputTable.addTableListener(this, true);
		NetworkTable.addGlobalConnectionListener(this, true);
	}

	@Override
	public void valueChanged(ITable iTable, String string, Object recievedObject, boolean newValue) {
		System.out.println("[client] String: "+string+" Value: "+recievedObject+" new: "+newValue);

		this.getContentPane().setBackground(Color.YELLOW);

		if(string.equals("waypoints")) {

			Waypoint[] waypoints = deserializeWaypointArray((String)recievedObject);
			double timeStep = pathfinderInputTable.getNumber("timeStep", 0.05);
			double maxVel = pathfinderInputTable.getNumber("maxVel", 10);
			double maxAccel  = pathfinderInputTable.getNumber("maxAccel", 20);
			double maxJerk = pathfinderInputTable.getNumber("maxJerk", 30);
			Trajectory trajectory = generatePath(waypoints, timeStep, maxVel, maxAccel, maxJerk);

			//output results to NT
			//genID is used for verification that the data is new, since if the same path object is output then valueChanged() doesn't fire.
			pathfinderOutputTable.putValue("path", serializeTrajectory(trajectory));
			pathfinderOutputTable.putValue("ID", genID);
			genID++;
		}

		this.getContentPane().setBackground(Color.GREEN);
	}

	public Trajectory generatePath(Waypoint[] waypoints, double timeStep, double maxVel, double maxAccel, double maxJerk) {
		Trajectory forwardTrajectory = null;
		try {	
			Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH,
					timeStep, maxVel, maxAccel, maxJerk);
			forwardTrajectory = Pathfinder.generate(waypoints, config);	
		} catch (Exception e) {}
		return forwardTrajectory;
	}

	public String serializeTrajectory(Trajectory trajectory) {
		String serializedTrajectory = ""; 
		
		try {
		     ByteArrayOutputStream bo = new ByteArrayOutputStream();
		     ObjectOutputStream so = new ObjectOutputStream(bo);
		     so.writeObject(trajectory);
		     so.flush();
		     serializedTrajectory = new String(Base64.getEncoder().encode(bo.toByteArray()));
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return serializedTrajectory;
	}

	public Waypoint[] deserializeWaypointArray(String serializedWaypoints) {
		Waypoint[] waypoints = null; 
		try {
		     byte[] b = Base64.getDecoder().decode(serializedWaypoints.getBytes()); 
		     ByteArrayInputStream bi = new ByteArrayInputStream(b);
		     ObjectInputStream si = new ObjectInputStream(bi);
		     waypoints = (Waypoint[]) si.readObject();
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return waypoints;
	}
	
	@Override
	public void connected(IRemote arg0) {
		this.getContentPane().setBackground(Color.GREEN);
		connected = true;
	}

	@Override
	public void disconnected(IRemote arg0) {
		this.getContentPane().setBackground(Color.RED);
		connected = false;
	}

}
