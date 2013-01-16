package nl.arnorob.point.view;

public class GonioUtil {
	
	/**
	 * // calculates angle between 0 and 360 where 0 is to the right and counting up clockwise
	 * 
	 * @param x
	 * @param y
	 * @return 0-360
	 */
	public static float getAngleDegrees(final float x, final float y) {
		final float tanAngle = (float) Math.toDegrees(Math.atan(y / x));
		// create angle between 0 and 360 where 0 is to right and counting up clockwise
		if (x >= 0) {
			if (y >= 0) {
				return tanAngle;
			} else {
				return 360 + tanAngle;
			}
		} else {
			return 180 + tanAngle;
		}

	}
}
