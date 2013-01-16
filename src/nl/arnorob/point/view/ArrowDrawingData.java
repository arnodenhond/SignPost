package nl.arnorob.point.view;

import android.util.Log;
import nl.arnorob.point.model.Trackable;

public class ArrowDrawingData implements Comparable<ArrowDrawingData> {
	/**
	 * 
	 * @param angle
	 *            angle in degrees from 0-360: 0 is down and up is counter clockwise
	 * @param length
	 *            in pixels from center
	 * @param t
	 */
	public ArrowDrawingData(float angle, float length, Trackable t) {
		this.angle = angle;
		this.length = length;
		this.t = t;
	}

	final float angle;
	final float length;
	final Trackable t;

	private float getAngleZeroRightUpClockwise() {
		// substract 90 to convert 0 from down to right
		// and convert from counterclockwise to clockwise
		float angleOisRight = -(angle - 90);

		// make sure angle is between 0 and 360
		 float angle0to360 = angleOisRight >= 360 ? angleOisRight - 360 : angleOisRight;
		 angle0to360 = angle0to360 < 0 ? angle0to360 + 360 : angle0to360;
		
		return angle0to360;
	}

	public boolean isClickOnArrow(float clickAngle, float clickDistance) {
		float convertedAngle=getAngleZeroRightUpClockwise();
		Log.i("ArrowDrawingData:" + t.name, " length;" + length + " angle " + convertedAngle);
		if (clickDistance <= length && Math.abs(clickAngle - convertedAngle) < 12)
			return true;
		else
			return false;
	}

	// sort on length
	public int compareTo(ArrowDrawingData another) {
		return (int) (length - another.length);
	}
}