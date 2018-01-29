import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.IRemote;
import edu.wpi.first.wpilibj.tables.IRemoteConnectionListener;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class RemotePathGenerator extends JFrame implements ITableListener, IRemoteConnectionListener {

	private static final long serialVersionUID = -3371908382388429620L;
	NetworkTable pathfinderInputTable;
	NetworkTable pathfinderOutputTable;
	boolean connected = false;
	int genID = 1;
	
	public static void main(String[] args) {
		NetworkTable.setClientMode();
		NetworkTable.setTeam(303);
		NetworkTable.setIPAddress("192.168.0.108"); //NT server address - will be RoboRIO's. TODO
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
		
		//simulate "work" (i.e. generating path)
		try {Thread.sleep(1000);} catch (InterruptedException e) {} 
		
		//output results to NT
		//genID is used for verification that the data is new, since if the same path object is output then valueChanged() doesn't fire.
		pathfinderOutputTable.putValue("path", "just pretend this is the path object");
		pathfinderOutputTable.putValue("ID", genID);
		genID++;
		
		this.getContentPane().setBackground(Color.GREEN);
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
