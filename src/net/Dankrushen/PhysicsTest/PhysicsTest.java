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
	static final LinkedHashMap<String, Float> FRICTIONS = new LinkedHashMap<String, Float>()
	{
		private static final long serialVersionUID = -902882376711498738L;

		{
			put("Rubber", 1f);
			put("Glass", 0.94f);
			put("Steel", 0.74f);
			put("Aluminum", 0.61f);
			put("Copper", 0.53f);
			put("Wood", 0.40f);
			put("Ice", 0.1f);
		}
	};

	public static float getIndexValue(int index) {
		if(index < 0 || index >= FRICTIONS.size())
			return 0;

		List<Entry<String, Float>> keys = new ArrayList<Entry<String, Float>>(FRICTIONS.entrySet());

		Entry<String, Float> curEntry = keys.get(index);

		return curEntry.getValue();
	}

	public PopUp(PhysicsTest parent){

		int curFric = parent.curFric;
		int nextFricIndex = (curFric < FRICTIONS.size() - 1 ? curFric + 1 : 0);
		int prevFricIndex = (curFric > 0 ? curFric - 1 : FRICTIONS.size() - 1);

		List<Entry<String, Float>> keys = new ArrayList<Entry<String, Float>>(FRICTIONS.entrySet());

		Entry<String, Float> curEntry = keys.get(curFric);
		Entry<String, Float> nextEntry = keys.get(nextFricIndex);
		Entry<String, Float> prevEntry = keys.get(prevFricIndex);

		JMenuItem curFricName = new JMenuItem("Current: " + curEntry.getKey() + " (" + curEntry.getValue() + ")");
		add(curFricName);

		addSeparator();

		JMenuItem nextFric = new JMenuItem("Next - " + nextEntry.getKey() + " (" + nextEntry.getValue() + ")");
		nextFric.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.curFric = nextFricIndex;
				parent.friction = nextEntry.getValue();
			}
		});
		add(nextFric);

		JMenuItem prevFric = new JMenuItem("Previous - " + prevEntry.getKey() + " (" + prevEntry.getValue() + ")");
		prevFric.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				parent.curFric = prevFricIndex;
				parent.friction = prevEntry.getValue();
			}
		});
		add(prevFric);

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
					parent.parent.unsetGravPoint();
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
	int frameLimit = Math.round(1f / ((float) targetFramerate) * 1000f);

	public float xScale = 1920f / 0.508f; // 1920 pixels / 20 inches wide or 0.508 meters
	public float yScale = 1080f / 0.28702f; // 1080 pixels / 11.3 inches high or 0.28702 meters
	int x = 0;
	int y = 0;

	int curFric = 3;
	float friction = PopUp.getIndexValue(curFric);

	boolean THROW = false;
	boolean LOCK_PHYS = false;
	boolean GRAV_POINT = false;
	private boolean dragging = false;
	double throwEvery = 3d * (double) targetFramerate; // Frames (in seconds)
	int framecount = 0;

	boolean regBuild = false;

	WindowManager parent;

	/**
	 * Create the application.
	 * @param parent 
	 */
	public PhysicsTest(WindowManager parent) {
		this.parent = parent;

		initialize();

		Point location = frame.getLocationOnScreen();
		windowPhys = new Vector2((float) location.getX(), (float) location.getY());
		windowPhys.invertX = false;
		windowPhys.invertY = true;

		moveWindow();

		Timer timer = new Timer(frameLimit,new ActionListener() {
			public void actionPerformed(ActionEvent event) 
			{
				setBounds();

				if(THROW && (double) framecount >= throwEvery) {
					Random random = new Random();
					windowPhys.addVelocity(((random.nextFloat() - 0.50f) * 0.80f) * xScale, (random.nextFloat() * 0.65f) * yScale);

					double mintime = 0.5d;
					double maxtime = 3d;
					throwEvery = ((random.nextDouble() * maxtime - mintime) + mintime) * (double) targetFramerate;
					framecount = 0;
				}

				//windowPhys.gravx = xScale * 0.62f; // Scale m/s2
				//windowPhys.gravy = yScale * 0.62f; // Scale m/s2
				windowPhys.grav = yScale * 0.05f;
				windowPhys.gravScalex = xScale;
				windowPhys.gravScaley = yScale;
				
				windowPhys.mass = 100f;

				windowPhys.fricFloor = xScale * friction;
				windowPhys.fricCeil = xScale * friction;

				windowPhys.fricLWall = yScale * friction;
				windowPhys.fricRWall = yScale * friction;

				windowPhys.addVelocity(0f, windowPhys.gravy * 0f);

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

		if(regBuild) {
			windowPhys.setLWall(0f);
			windowPhys.setRWall(width - frame.getWidth());

			windowPhys.setCeil(0);
			windowPhys.setFloor(height - frame.getHeight() - 40f);
		} else {
			windowPhys.setLWall(0f - 1280f);
			windowPhys.setRWall(width - frame.getWidth() + 1280f);

			Point location = frame.getLocationOnScreen();
			windowPhys.setCeil(0f);
			windowPhys.setFloor(height - frame.getHeight() - (location.getX() < 0d || location.getX() > (1920d - frame.getWidth()) ? 56f : 0f) - 40f);
		}
	}

	public void calculateMove() {
		if (LOCK_PHYS) {
			windowPhys.pausePhysics(true);
		} else if(windowPhys.pausePhysics != LOCK_PHYS && !dragging) {
			windowPhys.pausePhysics(false);
		}

		float[] oldPos = windowPhys.getPos();
		float[] newPos = windowPhys.move();

		int disX = Math.round(newPos[0]) - Math.round(oldPos[0]);
		int disY = Math.round(newPos[1]) - Math.round(oldPos[1]);

		//System.out.println("Displacement: (" + disX + ", " + disY + ")");

		//this.x = Math.round(newPos[0]);
		//this.y = Math.round(newPos[1]);

		if(frame.isVisible()) {
			Point location = frame.getLocationOnScreen();
			this.x = location.x + disX;
			this.y = location.y + disY;
		}

		float[] oob = windowPhys.outOfBounds(this.x, this.y);
		this.x = Math.round(oob[0]);
		this.y = Math.round(oob[1]);

		windowPhys.setPos((float) this.x, (float) this.y);

		if(!windowPhys.pausePhysics)
			moveWindow();
		else windowPhys.setVelocity((oldPos[0] - this.x) * frameLimit, (oldPos[1] - this.y) * frameLimit);
		
		if(GRAV_POINT)
			parent.setGravPoint(this, this.x, this.y, this.windowPhys.mass);

		drawColour(Math.abs(windowPhys.velx / xScale) + Math.abs(windowPhys.vely / yScale));
	}

	public void moveWindow() {
		frame.setLocation(this.x, this.y);
	}

	public void drawColour(float netVel) {
		//System.out.println(netVel);
		DecimalFormat roundVel = new DecimalFormat("0.0");
		lblSpeed.setText(roundVel.format(netVel) + " m/s");

		frame.getContentPane().setBackground(new Color(((netVel = (netVel < 0 ? 0 : netVel)) > 1 ? 1 : netVel), 0f, 0f));
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
			parent.unsetGravPoint();
		}
		
		this.frame.setVisible(false);
		this.frame.dispose();
	}

}
