package nl.arnorob.point.test;

import android.location.Location;
import android.location.LocationManager;
import nl.arnorob.point.model.Trackable;

public class MockTrackable extends Trackable {

	private float mockdistance;
	private float mockbearing;
	
	public MockTrackable(int color, String name, float distance, float bearing) {
		super(name,color,new Location(LocationManager.NETWORK_PROVIDER));
		mockdistance = distance;
		mockbearing = bearing;
	}

	@Override
	public float getBearing() {
		return mockbearing;
	}
	
	@Override
	public float getDistance() {
		return mockdistance;
	}

	public int compareTo(Trackable other) {
		return (int) (other.getDistance() - getDistance());
	}
	
}
