package net.Dankrushen.PhysicsTest;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

public class WindowManager {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new WindowManager();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	List<PhysicsTest> windows = new ArrayList<PhysicsTest>();
	
	public WindowManager() {
		windows.add(new PhysicsTest(this));
	}

	public void setGravPoint(PhysicsTest window, int x, int y, float mass) {
		for (PhysicsTest target : windows) {
			target.windowPhys.addGravityPoint(window.windowPhys);
			target.windowPhys.gravMass = mass;
			target.windowPhys.pointGrav = true;
		}
	}
	
	public void unsetGravPoint(PhysicsTest window) {
		for (PhysicsTest target : windows) {
			target.windowPhys.removeGravityPoint(window.windowPhys);
			if(target.windowPhys.posGrav.isEmpty())
				target.windowPhys.pointGrav = false;
		}
	}
	
	public void newWindow() {
		windows.add(new PhysicsTest(this));
	}
	
	public void closeWindow(PhysicsTest window) {
		window.close();
		
		if(windows.contains(window))
			windows.remove(window);
		
		if(windows.isEmpty()) {
			System.exit(0);
		}
	}
}
