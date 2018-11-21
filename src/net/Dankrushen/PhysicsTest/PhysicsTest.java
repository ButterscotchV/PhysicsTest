package net.Dankrushen.PhysicsTest;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import java.awt.Font;
import javax.swing.SwingConstants;

class PopUp extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	static final LinkedHashMap<String, Double> FRICTIONS = new LinkedHashMap<String, Double>()
	{
		private static final long serialVersionUID = -902882376711498738L;

		{
			put("Rubber", 1d);
			put("Steel", 0.74d);
			put("Glass", 0.6d);
			put("Aluminum", 0.47d);
			put("Wood", 0.45d);
			put("Copper", 0.36d);
			put("Graphite", 0.1d);
			put("Teflon", 0.04d);
						
			/*
			put("Rubber", 1000d);
			put("Steel", 7400d);
			put("Glass", 600d);
			put("Aluminum", 4700d);
			put("Wood", 4500d);
			put("Copper", 3600d);
			put("Graphite", 100d);
			put("Teflon", 40d);
			*/
		}
	};

	public static double getIndexValue(int index) {
		if(index < 0 || index >= FRICTIONS.size())
			return 0;

		List<Entry<String, Double>> keys = new ArrayList<Entry<String, Double>>(FRICTIONS.entrySet());

		Entry<String, Double> curEntry = keys.get(index);

		return curEntry.getValue();
	}

	public PopUp(PhysicsTest parent){
		JMenuItem fric = new JMenuItem("Friction:");
		add(fric);
		
		List<Entry<String, Double>> keys = new ArrayList<Entry<String, Double>>(FRICTIONS.entrySet());

		for(int i = 0; i < FRICTIONS.size(); i++) {
			final int index = i;
			final Entry<String, Double> curEntry = keys.get(index);
			
			JCheckBoxMenuItem fricItem = new JCheckBoxMenuItem(curEntry.getKey() + " (" + curEntry.getValue() + ")");
			fricItem.setSelected(index == parent.curFric);
			fricItem.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					parent.curFric = index;
					parent.friction = curEntry.getValue();
				}
			});
			add(fricItem);
		}

		addSeparator();

		JCheckBoxMenuItem jump = new JCheckBoxMenuItem("Random Jumping");
		jump.setSelected(parent.THROW);
		jump.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.THROW = !parent.THROW;
			}
		});
		add(jump);

		JCheckBoxMenuItem lock = new JCheckBoxMenuItem("Anchor");
		lock.setSelected(parent.LOCK_PHYS);
		lock.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.LOCK_PHYS = !parent.LOCK_PHYS;
			}
		});
		add(lock);

		JCheckBoxMenuItem gravPoint = new JCheckBoxMenuItem("Gravity Point");
		gravPoint.setSelected(parent.GRAV_POINT);
		gravPoint.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.GRAV_POINT = !parent.GRAV_POINT;

				if(!parent.GRAV_POINT) {
					parent.parent.unsetGravPoint(parent);
				}
			}
		});
		add(gravPoint);

		addSeparator();

		JMenuItem open = new JMenuItem("New Window");
		open.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.parent.newWindow();
			}
		});
		add(open);

		JMenuItem close = new JMenuItem("Close");
		close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.parent.closeWindow(parent);
			}
		});
		add(close);

		JMenuItem exit = new JMenuItem("Exit");
		exit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				System.exit(0);
			}
		});
		add(exit);
	}
}

public class PhysicsTest {

	private JFrame frame;
	private JLabel lblSpeed;
	public Vector2 windowPhys;

	int targetFramerate = 60;
	int frameLimit = (int) Math.round(1d / (targetFramerate) * 1000d);

	public double scale = 1080d / 0.28702d; // 1080 pixels / 11.3 inches high or 0.28702 meters
	int x = 0;
	int y = 0;

	int xPointDiff = 0;
	int yPointDiff = 0;
	
	int curFric = 2;
	double friction = PopUp.getIndexValue(curFric);

	boolean THROW = false;
	boolean LOCK_PHYS = false;
	boolean GRAV_POINT = false;
	boolean dragging = false;
	boolean popup = false;
	double throwEvery = 3d * targetFramerate; // Frames (in seconds)
	int framecount = 0;

	boolean regBuild = true;

	WindowManager parent;

	/**
	 * Create the application.
	 * @param parent 
	 */
	public PhysicsTest(WindowManager parent) {
		this.parent = parent;

		initialize();

		Point location = frame.getLocationOnScreen();
		windowPhys = new Vector2(location.getX(), location.getY());
		windowPhys.invertY = true;

		moveWindow();

		Timer timer = new Timer(frameLimit,new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) 
			{
				setBounds();

				if(THROW && framecount >= throwEvery) {
					Random random = new Random();
					windowPhys.addVelocity(((random.nextDouble() - 0.50d) * 0.80d) * scale, (random.nextDouble() * 0.65d) * scale);

					double mintime = 0.5d;
					double maxtime = 3d;
					throwEvery = ((random.nextDouble() * maxtime - mintime) + mintime) * targetFramerate;
					framecount = 0;
				}

				//windowPhys.gravx = xScale * 0.62d; // Scale m/s2
				//windowPhys.gravy = yScale * 0.62d; // Scale m/s2
				windowPhys.grav = scale * 0.62d;
				windowPhys.gravScale = scale;

				// windowPhys.timeScale = 0.25d;
				
				windowPhys.fricFloor = scale * friction;
				windowPhys.fricCeil = scale * friction;

				windowPhys.fricLWall = scale * friction;
				windowPhys.fricRWall = scale * friction;

				// windowPhys.addVelocity(0d, windowPhys.gravy * 0d);

				calculateMove();
				framecount++;
				//System.out.println("Position: (" + x + ", " + y + ")");
			}
		});

		timer.setRepeats(true);

		timer.start();
	}

	public void setBounds() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		xPointDiff = (int) Math.round(frame.getWidth() / 2d);
		yPointDiff = (int) Math.round(frame.getHeight() / 2d);

		if(frame.isVisible()) {
			windowPhys.mass = Math.pow(1.03, (frame.getWidth() + frame.getHeight())) * Math.pow(10, 8);
			//System.out.println(windowPhys.mass);
		}
		
		if(regBuild) {
			windowPhys.setLWall(0d + xPointDiff);
			windowPhys.setRWall(width - frame.getWidth() + xPointDiff);

			windowPhys.setCeil(0d + yPointDiff);
			windowPhys.setFloor(height - frame.getHeight() - 40d + yPointDiff);
		} else {
			windowPhys.setLWall(0d - 1280d + xPointDiff);
			windowPhys.setRWall(width - frame.getWidth() + 1280d + xPointDiff);

			if(frame.isVisible()) {
				Point location = frame.getLocationOnScreen();
				windowPhys.setCeil(0d + yPointDiff);
				windowPhys.setFloor(height - frame.getHeight() - (location.getX() < 0d || location.getX() > (1920d - frame.getWidth()) ? 56d : 0d) - 40d + yPointDiff);
			}
		}
	}

	public void calculateMove() { // Definite loss of accuracy here?
		if (LOCK_PHYS) {
			windowPhys.pausePhysics(true);
		} else if(windowPhys.pausePhysics != LOCK_PHYS && !dragging && !popup) {
			windowPhys.pausePhysics(false);
		}

		double[] oldPos = windowPhys.getPos();
		double[] newPos = windowPhys.move();

		//System.out.println("Displacement: (" + disX + ", " + disY + ")");

		//this.x = Math.round(newPos[0]);
		//this.y = Math.round(newPos[1]);

		this.x = (int) Math.round(newPos[0]);// + this.xPointDiff;
		this.y = (int) Math.round(newPos[1]);// + this.yPointDiff;

		if(!windowPhys.pausePhysics)
			moveWindow();
		else {
			if(frame.isVisible()) {
				Point location = frame.getLocationOnScreen();
				this.x = location.x + this.xPointDiff;
				this.y = location.y + this.yPointDiff;
			}
			
			windowPhys.setPos(this.x, this.y);
			windowPhys.setVelocity((oldPos[0] - this.x) * frameLimit, (oldPos[1] - this.y) * frameLimit);
		}

		if(GRAV_POINT)
			parent.setGravPoint(this);

		drawColour(Math.abs(windowPhys.velx / scale) + Math.abs(windowPhys.vely / scale));
	}

	public void moveWindow() {
		frame.setLocation(this.x - this.xPointDiff, this.y - this.yPointDiff);
	}

	public void drawColour(double netVel) {
		//System.out.println(netVel);
		DecimalFormat roundVel = new DecimalFormat("0.0");
		lblSpeed.setText(roundVel.format(netVel) + " m/s");

		frame.getContentPane().setBackground(new Color((float) ((netVel = (netVel < 0 ? 0 : netVel)) > 1 ? 1 : netVel), 0f, 0f));
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setUndecorated(true);
		frame.setBounds(100, 100, 200, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				windowPhys.pausePhysics(true);
				dragging = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				windowPhys.pausePhysics(false);
				dragging = false;
			}
		});

		CustomResizer.install(frame, this);

		lblSpeed = new JLabel();
		lblSpeed.setHorizontalAlignment(SwingConstants.CENTER);
		lblSpeed.setForeground(Color.WHITE);
		lblSpeed.setFont(new Font("Ubuntu", Font.BOLD, 45));
		frame.getContentPane().add(lblSpeed);

		frame.setVisible(true);
	}

	public void close() {
		if(GRAV_POINT) {
			GRAV_POINT = false;
			parent.unsetGravPoint(this);
		}

		this.frame.setVisible(false);
		this.frame.dispose();
	}

}
