package net.Dankrushen.PhysicsTest;

import java.util.ArrayList;
import java.util.List;

public class Vector2 {
	double grav = 9.81d;

	double gravx = 0d;
	double gravy = grav;

	boolean pointGrav = false;
	List<Vector2> posGrav = new ArrayList<Vector2>();
	double gravScale = 1d;

	double posx = 0d;
	double posy = 0d;

	double velx = 0d;
	double vely = 0d;

	double accelx = 0d;
	double accely = 0d;

	double mass = 1d;

	boolean invertX = false;
	boolean invertY = false;

	boolean useFloor = false;
	double floor = 0d;
	boolean useCeil = false;
	double ceil = 0d;
	boolean useLWall = false;
	double lWall = 0d;
	boolean useRWall = false;
	double rWall = 0d;

	double fricFloor = 0d;
	double fricCeil = 0d;
	double fricLWall = 0d;
	double fricRWall = 0d;

	double timeScale = 1d;
	long lastMoved = System.currentTimeMillis();

	boolean pausePhysics;

	public Vector2(double posx, double posy) {
		this.posx = posx;
		this.posy = posy;
	}

	public Vector2(double posx, double posy, double velx, double vely) {
		this.posx = posx;
		this.posy = posy;

		this.velx = velx;
		this.vely = vely;
	}

	public Vector2(double posx, double posy, double velx, double vely, double accelx, double accely) {
		this.posx = posx;
		this.posy = posy;

		this.velx = velx;
		this.vely = vely;

		this.accelx = accelx;
		this.accely = accely;
	}

	public void setVelocity(double velx, double vely) {
		this.velx = velx;
		this.vely = vely;
	}

	public void addVelocity(double velx, double vely) {
		this.velx += velx;
		this.vely += vely;
	}

	public void subVelocity(double velx, double vely) {
		this.velx -= velx;
		this.vely -= vely;
	}

	public void setAccel(double accelx, double accely) {
		this.accelx = accelx;
		this.accely = accely;
	}

	public void addAccel(double accelx, double accely) {
		this.accelx += accelx;
		this.accely += accely;
	}

	public void subAccel(double accelx, double accely) {
		this.accelx -= accelx;
		this.accely -= accely;
	}
	
	public void addGravityPoint(Vector2 source) {
		if(!this.posGrav.contains(source)) {
			this.posGrav.add(source);
		}
	}
	
	public void removeGravityPoint(Vector2 source) {
		if(this.posGrav.contains(source)) {
			this.posGrav.remove(source);
		}
	}

	public double[] move() {
		long curTime = System.currentTimeMillis();
		double interval = ((curTime - this.lastMoved) / 1000d) * timeScale;
		this.lastMoved = curTime;

		if(this.pointGrav) {
			this.gravx = 0d;
			this.gravy = 0d;
			for(Vector2 source : this.posGrav) {
				double[] sourcePos = source.getPos();
				double[] gravs = this.setGravitytoPoint(source.mass, sourcePos[0], sourcePos[1]);
				this.gravx += gravs[0];
				this.gravy += gravs[1];
			}
		} else {
			this.gravx = 0d;
			this.gravy = this.grav;
		}

		if (!this.pausePhysics)
			return new double[] {this.moveX(interval), this.moveY(interval)};
		else
			return new double[] {this.posx, this.posy};
	}

	public double moveX(double seconds) {
		fricX(seconds);

		this.velx += this.getVelDiff(this.accelx + this.gravx, seconds);

		if(!this.invertX) this.posx -= this.displacement( this.velx, seconds);
		else this.posx += this.displacement( this.velx, seconds);

		double[] boundx = boundX(this.posx, this.velx);
		this.posx = boundx[0];
		this.velx = boundx[1];

		return this.posx;
	}

	public double moveY(double seconds) {
		fricY(seconds);

		this.vely += this.getVelDiff(this.accely - this.gravy, seconds);

		if(!this.invertY) this.posy += this.displacement(this.vely, seconds);
		else this.posy -= this.displacement(this.vely, seconds);

		double[] boundy = boundY(this.posy, this.vely);
		this.posy = boundy[0];
		this.vely = boundy[1];

		return this.posy;
	}

	public void fricY(double seconds) {
		if ((this.useFloor && (this.invertY ? this.posy >= this.floor : this.posy <= this.floor)) || (this.useCeil && (this.invertY ? this.posy <= this.ceil : this.posy >= this.ceil))) {
			double fric = (this.useFloor && this.posy <= this.floor ? this.fricFloor : this.fricCeil) * seconds;

			if(fric > Math.abs(this.velx))
				this.velx = 0;

			if(this.velx > 0) {
				this.velx -= fric;
			}
			else if(this.velx < 0) {
				this.velx += fric;
			}
		}
	}

	public void fricX(double seconds) {
		if ((this.useLWall && (this.invertX ? this.posx >= this.lWall : this.posx <= this.lWall)) || (this.useRWall && (this.invertX ? this.posx <= this.rWall : this.posx >= this.rWall))) {
			double fric = (this.useLWall && this.posx <= this.lWall ? this.fricLWall : this.fricRWall) * seconds;

			if(fric > Math.abs(this.vely))
				this.vely = 0;

			if(this.vely > 0) {
				this.vely -= fric;
			}
			else if(this.vely < 0) {
				this.vely += fric;
			}
		}
	}

	public double[] boundY(double yVal, double yVel) {
		if (this.useFloor && ((!this.invertY && yVal < this.floor) || (this.invertY && yVal > this.floor))) {
			yVal = this.floor;
			yVel = 0d;
		}

		if (this.useCeil && ((!this.invertY && yVal > this.ceil) || (this.invertY && yVal < this.ceil))) {
			yVal = this.ceil;
			yVel = 0d;
		}

		return new double[] {yVal, yVel};
	}

	public double[] boundX(double xVal, double xVel) {
		if (this.useLWall && ((!this.invertX && xVal < this.lWall) || (this.invertX && xVal > this.lWall))) {
			xVal = this.lWall;
			xVel = 0d;
		}

		if (this.useRWall && ((!this.invertX && xVal > this.rWall) || (this.invertX && xVal < this.rWall))) {
			xVal = this.rWall;
			xVel = 0d;
		}

		return new double[] {xVal, xVel};
	}

	public double displacement(double velFinal, double seconds) {
		return velFinal * seconds;
	}

	public double getVelDiff(double accel, double seconds) {
		return accel * seconds;
	}

	public void setFloor(double floor) {
		this.useFloor = true;
		this.floor = floor;
	}

	public void setCeil(double ceil) {
		this.useCeil = true;
		this.ceil = ceil;
	}

	public void setLWall(double lWall) {
		this.useLWall = true;
		this.lWall = lWall;
	}

	public void setRWall(double rWall) {
		this.useRWall = true;
		this.rWall = rWall;
	}

	public double[] getPos() {
		return new double[] {this.posx, this.posy};
	}

	public void setPos(double x, double y) {
		this.posx = x;
		this.posy = y;

		double[] boundx = boundX(this.posx, this.velx);
		this.posx = boundx[0];
		this.velx = boundx[1];

		double[] boundy = boundY(this.posy, this.vely);
		this.posy = boundy[0];
		this.vely = boundy[1];
	}

	public void pausePhysics(boolean pause) {
		this.pausePhysics = pause;
		if(pause) {
			this.velx = 0d;
			this.vely = 0d;
		}
	}

	public double[] outOfBounds(int x, int y) {
		double[] boundx = boundX(x, 0d);
		double[] boundy = boundY(y, 0d);

		return new double[] {boundx[0], boundy[0]};
	}

	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}

	public double distanceFrom(double x, double y) {		
		return Math.sqrt(Math.pow(Math.abs(x - this.posx), 2) + Math.pow(Math.abs(y - this.posy), 2));
	}

	public double[] setGravitytoPoint(double gravMass, double xPos, double yPos) {
		// grav = hypotenuse
				// angle = ???

		double dist = this.distanceFrom(xPos, yPos);
		double diffx = xPos - this.posx;
		double diffy = yPos - this.posy;

		// hypotenuse = dist
		// opposite = diffx
		// adjacent = diffy

		double grav = ((6.67408d * Math.pow(10, -11)) * ((gravMass * this.mass) / Math.pow(dist, 2))) / this.mass;
		double maxGrav = 15000d;
		grav = (grav > maxGrav ? maxGrav : grav);

		if(diffx == 0d && diffy == 0d)
			return new double[] {0d, 0d};

		double angle = Math.atan(Math.abs(diffy) / Math.abs(diffx));

		// angle = 90d - angle; // Reverse value (turn to clockwise)

		// System.out.println(diffx + ", " + diffy + " dir: " + direction + " ang: " + Math.toDegrees(angle));

		// System.out.println(Math.tan(angle) / grav);

		double xGrav = grav * Math.cos(angle);
		double yGrav = grav * Math.sin(angle);

		Direction direction = Direction.WEST;

		if(diffx < 0d && diffy >= 0d)
			direction = Direction.NORTH;
		else if(diffx >= 0d && diffy < 0d)
			direction = Direction.SOUTH;
		else if(diffx <= 0d && diffy < 0d)
			direction = Direction.EAST;

		switch(direction) {
		case NORTH:
			break;
		case EAST:
			yGrav = -yGrav;
			break;
		case SOUTH:
			xGrav = -xGrav;
			yGrav = -yGrav;
			break;
		case WEST:
			xGrav = -xGrav;
			break;
		}

		//System.out.println("xGrav: " + xGrav + " yGrav: " + yGrav + " total: " + Math.sqrt(Math.pow(xGrav, 2) + Math.pow(yGrav, 2)));

		return new double[] {xGrav, yGrav};
	}
}
