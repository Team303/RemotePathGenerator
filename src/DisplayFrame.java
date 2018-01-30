import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class DisplayFrame extends JFrame{
	private static final long serialVersionUID = -924699517006012122L;

	public DisplayFrame() {
		this.setTitle("Remote Path Generator Status");
		this.setSize(450, 300);
		setBackground(Color.RED);
		this.setAlwaysOnTop(true);
		this.setUndecorated(true);
		this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		//this.setOpacity(0.5f);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

	}

	/**
	 * Sets the background of the frame. Display color codes: <ul>
	 * 		<li>RED: NT Disconnected</li> 
	 * 		<li>GREEN: NT Connected</li> 
	 * 		<li>YELLOW: Currently generating trajectories</li> 
	 * 		<li>BLACK: Fatal error, one or more trajectories failed to generate</li> 
	 */
	public void setBackground(Color color) {
		this.getContentPane().setBackground(color);
	}
	
	public Color getBackground() {
		return this.getContentPane().getBackground();
	}
	
}
