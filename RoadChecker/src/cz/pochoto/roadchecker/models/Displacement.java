package cz.pochoto.roadchecker.models;

import android.location.Location;

public class Displacement {
	private Location loc;
	private long time;
	private double value;
	private boolean onMap = false;
	
	public Displacement(long time, double value) {
		super();
		this.time = time;
		this.value = value;
	}
	
	public Displacement(Location loc, long time, double value) {
		super();
		this.loc = loc;
		this.time = time;
		this.value = value;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isOnMap() {
		return onMap;
	}

	public void setOnMap(boolean onMap) {
		this.onMap = onMap;
	}
}
