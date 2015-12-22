package com.game.bee;

public class Bee {
	private int x;
	private int y;
	private int attackNum;
	private int speed;
	private BeeType type;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getAttackNum() {
		return attackNum;
	}

	public void setAttackNum(int attackNum) {
		this.attackNum = attackNum;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public BeeType getType() {
		return type;
	}

	public void setType(BeeType type) {
		this.type = type;
	}

}
