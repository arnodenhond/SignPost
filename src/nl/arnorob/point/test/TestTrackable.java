package nl.arnorob.point.test;

import nl.arnorob.point.model.Trackable;
import android.graphics.Color;

public class TestTrackable {

//	public static Trackable[] getTrackables() {
//		Trackable[] tr = new Trackable[6];
//
//		Trackable t1 = new Trackable(Color.BLUE, "Home");
//		Location location1 = new Location("point");
//		location1.setLatitude(52.160810);
//		location1.setLongitude(4.468964);
//		t1.setLocation(location1);
//		tr[0] = t1;
//		Trackable t2 = new Trackable(Color.CYAN, "Google HQ");
//		Location location2 = new Location("point");
//		location2.setLatitude(37.421972);
//		location2.setLongitude(-122.084143);
//		t2.setLocation(location2);
//		tr[1] = t2;
//		Trackable t3 = new Trackable(Color.YELLOW, "Taj Mahal");
//		Location location3 = new Location("point");
//		location3.setLatitude(27.172773);
//		location3.setLongitude(78.041655);
//		t3.setLocation(location3);
//		tr[2] = t3;
//		Trackable t4 = new Trackable(Color.RED, "London");
//		Location location4 = new Location("point");
//		location4.setLatitude(52.5);
//		location4.setLongitude(-0.12);
//		t4.setLocation(location4);
//		tr[3] = t4;
//		Trackable t5 = new Trackable(Color.LTGRAY, "Berlin");
//		Location location5 = new Location("point");
//		location5.setLatitude(52.5);
//		location5.setLongitude(13.4);
//		t5.setLocation(location5);
//		tr[4] = t5;
//
//		Trackable t6 = new Trackable(Color.LTGRAY, "Kaapstad");
//		Location location6 = new Location("point");
//		location6.setLatitude(-33.9);
//		location6.setLongitude(18.4);
//		t6.setLocation(location6);
//		tr[5] = t6;
//
//		for (Trackable trackable : tr) {
//			trackable.update(location1);
//		}
//
//		return tr;
//	}

	private static String getDistanceString(int distance) {
		if (distance < 10000)
			return distance + " m";
		else
			return distance / 1000 + " km";
	}

	public static Trackable[] getTrackables() {
		MockTrackable[] tr = new MockTrackable[7];

		float bearingstep = 360 / 7;
		for (int i = 0; i < 7; i++) {
			double distance = Math.pow(10,i+1);
			MockTrackable mt = new MockTrackable(Color.RED, getDistanceString((int) distance), (float) distance, i * bearingstep);
			tr[i] = mt;
		}

		return tr;
	}

}
