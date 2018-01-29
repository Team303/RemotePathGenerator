
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class NetworkTablesTester {

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
		
		while(!Thread.interrupted()) {
			
			pathfinderInputTable.putValue("waypoints", ""+(int)(Math.random()*100));
			NetworkTable.flush();
			
			Thread.sleep(5000);
		}
		
	}

}
