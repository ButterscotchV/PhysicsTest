package net.Dankrushen.PhysicsTest;

public class Vector2 {
	float grav = 9.81f;
	
	float gravx = 0f;
	float gravy = grav;
	
	boolean pointGrav = false;
	float posGravx = 0f;
	float posGravy = 0f;
	float gravScale = 1f;
	float gravMass = 0f;

	float posx = 0f;
	float posy = 0f;

	float velx = 0f;
	float vely = 0f;

	float accelx = 0f;
	float accely = 0f;
	
	float mass = 0f;

	boolean invertX = false;
	boolean invertY = false;

	boolean useFloor = false;
	float floor = 0f;
	boolean useCeil = false;
	float ceil = 0f;
	boolean useLWall = false;
	float lWall = 0f;
	boolean useRWall = false;
	float rWall = 0f;
	
	float fricFloor = 0f;
	float fricCeil = 0f;
	float fricLWall = 0f;
	float fricRWall = 0f;

	long lastMoved = System.currentTimeMillis();

	boolean pausePhysics;

	public Vector2(float posx, float posy) {
		this.posx = posx;
		this.posy = posy;
	}

	public Vector2(float posx, float posy, float velx, float vely) {
		this.posx = posx;
		this.posy = posy;

		this.velx = velx;
		this.vely = vely;
	}

	public Vector2(float posx, float posy, float velx, float vely, float accelx, float accely) {
		this.posx = posx;
		this.posy = posy;

		this.velx = velx;
		this.vely = vely;

		this.accelx = accelx;
		this.accely = accely;
	}
	
	public void setVelocity(float velx, float vely) {
		this.velx = velx;
		this.vely = vely;
	}

	public void addVelocity(float velx, float vely) {
		this.velx += velx;
		this.vely += vely;
	}

	public void subVelocity(float velx, float vely) {
		this.velx -= velx;
		this.vely -= vely;
	}

	public void setAccel(float accelx, float accely) {
		this.accelx = accelx;
		this.accely = accely;
	}
	
	public void addAccel(float accelx, float accely) {
		this.accelx += accelx;
		this.accely += accely;
	}

	public void subAccel(float accelx, float accely) {
		this.accelx -= accelx;
		this.accely -= accely;
	}

	public float[] move() {
		long curTime = System.currentTimeMillis();
		float interval = (curTime - this.lastMoved) / 1000f;
		this.lastMoved = curTime;
		
		if(this.pointGrav) {
			float[] gravs = this.setGravitytoPoint(this.grav, this.posGravx, this.posGravy);
			this.gravx = gravs[0];
			this.gravy = gravs[1];
		} else {
			this.gravx = 0f;
			this.gravy = this.grav;
		}

		if (!this.pausePhysics)
			return new float[] {this.moveX(interval), this.moveY(interval)};
		else
			return new float[] {this.posx, this.posy};
	}

	public float moveX(float seconds) {
		fricX(seconds);
		
		float velInit = this.velx;
		this.velx += this.getVelDiff(this.accelx + this.gravx, seconds);

		if(!this.invertX) this.posx -= this.displacement(velInit, this.velx, seconds);
		else this.posx += this.displacement(velInit, this.velx, seconds);

		float[] boundx = boundX(this.posx, this.velx);
		this.posx = boundx[0];
		this.velx = boundx[1];
		
		return this.posx;
	}

	public float moveY(float seconds) {
		fricY(seconds);
		
		float velInit = this.vely;
		this.vely += this.getVelDiff(this.accely - this.gravy, seconds);

		if(!this.invertY) this.posy += this.displacement(velInit, this.vely, seconds);
		else this.posy -= this.displacement(velInit, this.vely, seconds);

		float[] boundy = boundY(this.posy, this.vely);
		this.posy = boundy[0];
		this.vely = boundy[1];

		return this.posy;
	}
	
	public void fricY(float seconds) {
		if ((this.useFloor && (this.invertY ? this.posy >= this.floor : this.posy <= this.floor)) || (this.useCeil && (this.invertY ? this.posy <= this.ceil : this.posy >= this.ceil))) {
			float fric = (this.useFloor && this.posy <= this.floor ? this.fricFloor : this.fricCeil) * seconds;
			
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
	
	public void fricX(float seconds) {
		if ((this.useLWall && (this.invertX ? this.posx >= this.lWall : this.posx <= this.lWall)) || (this.useRWall && (this.invertX ? this.posx <= this.rWall : this.posx >= this.rWall))) {
			float fric = (this.useLWall && this.posx <= this.lWall ? this.fricLWall : this.fricRWall) * seconds;
			
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

	public float[] boundY(float yVal, float yVel) {
		if (this.useFloor && ((!this.invertY && yVal < this.floor) || (this.invertY && yVal > this.floor))) {
			yVal = this.floor;
			yVel = 0f;
		}
		
		if (this.useCeil && ((!this.invertY && yVal > this.ceil) || (this.invertY && yVal < this.ceil))) {
			yVal = this.ceil;
			yVel = 0f;
		}
		
		return new float[] {yVal, yVel};
	}

	public float[] boundX(float xVal, float xVel) {
		if (this.useLWall && ((!this.invertX && xVal < this.lWall) || (this.invertX && xVal > this.lWall))) {
			xVal = this.lWall;
			xVel = 0f;
		}
		
		if (this.useRWall && ((!this.invertX && xVal > this.rWall) || (this.invertX && xVal < this.rWall))) {
			xVal = this.rWall;
			xVel = 0f;
		}
		
		return new float[] {xVal, xVel};
	}
	
	public float displacement(float velInit, float velFinal, float seconds) {
		return ((velInit + velFinal) / 2) * seconds;
	}

	public float getVelDiff(float accel, float seconds) {
		return accel * seconds;
	}

	public void setFloor(float floor) {
		this.useFloor = true;
		this.floor = floor;
	}
	
	public void setCeil(float ceil) {
		this.useCeil = true;
		this.ceil = ceil;
	}
	
	public void setLWall(float lWall) {
		this.useLWall = true;
		this.lWall = lWall;
	}
	
	public void setRWall(float rWall) {
		this.useRWall = true;
		this.rWall = rWall;
	}

	public float[] getPos() {
		return new float[] {this.posx, this.posy};
	}

	public void setPos(float x, float y) {
		this.posx = x;
		this.posy = y;

		float[] boundx = boundX(this.posx, this.velx);
		this.posx = boundx[0];
		this.velx = boundx[1];
		
		float[] boundy = boundY(this.posy, this.vely);
		this.posy = boundy[0];
		this.vely = boundy[1];
	}

	public void pausePhysics(boolean pause) {
		this.pausePhysics = pause;
		if(pause) {
			this.velx = 0f;
			this.vely = 0f;
		}
	}

	public float[] outOfBounds(int x, int y) {
		float[] boundx = boundX(x, 0f);
		float[] boundy = boundY(y, 0f);
		
		return new float[] {boundx[0], boundy[0]};
	}
	
	private enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}
	
	public float[] setGravitytoPoint(float grav, float xPos, float yPos) {
		// grav = hypotenuse
		// angle = ???
		
		float diffx = xPos - this.posx;
		float diffy = yPos - this.posy;
		
		// opposite = diffx
		// adjacent = diffy
		
		grav = (grav * this.mass) / (Math.abs(diffy) + Math.abs(diffx));
		//grav = Math.abs(grav);
		
		if(diffx == 0f && diffy == 0f)
			return new float[] {0f, 0f};
		
		Direction direction = Direction.WEST;
		
		if(diffx < 0f && diffy >= 0f)
			direction = Direction.NORTH;
		else if(diffx >= 0f && diffy < 0f)
			direction = Direction.SOUTH;
		else if(diffx < 0f && diffy < 0f)
			direction = Direction.EAST;
		
		double angle = Math.atan(Math.abs(diffy) / Math.abs(diffx));
		
		// angle = 90f - angle; // Reverse value (turn to clockwise)
		
		// System.out.println(diffx + ", " + diffy + " dir: " + direction + " ang: " + Math.toDegrees(angle));
		
		// System.out.println(Math.tan(angle) / grav);
		
		float xGrav = grav * (float) Math.cos(angle);
		float yGrav = grav * (float) Math.sin(angle);
		
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
		
		// System.out.println("xGrav: " + xGrav + " yGrav: " + yGrav + " total: " + Math.sqrt(Math.pow(xGrav, 2) + Math.pow(yGrav, 2)));
		
		return new float[] {xGrav, yGrav};
	}
}
