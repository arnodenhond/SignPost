package nl.arnorob.pointlite.model;

import android.location.Location;

public class Trackable implements Comparable<Trackable> {

	public final String name;
	public final int color;
	private Location location;

	private float distance;
	private float bearing;
	private final long id;

	public Trackable(String name, int color, Location location) {
		this(name, color, location, -1l);
	}

	public Trackable(String name, int color, Location location, long id) {
		this.name = name;
		this.color = color;
		this.location = location;
		this.id = id;
	}

	/**
	 * Updates bearing and distance compared to current location.
	 * 
	 * @param fromLocation
	 *            current location.
	 */
	public synchronized void update(Location fromLocation) {
		if (fromLocation != null) {
			bearing = fromLocation.bearingTo(location);
			distance = fromLocation.distanceTo(location);
		}
	}

	public float getDistance() {
		return distance;
	}

	public float getBearing() {
		return bearing;
	}

	public int compareTo(Trackable other) {
		return (int) (other.getDistance() - getDistance());
	}

	public String toString() {
		return "trackable " + this.hashCode() + name + "; direction:" + bearing + "; dist:" + distance + " at " + location;
	}

	public long getDbId() {
		return id;
	}

}
