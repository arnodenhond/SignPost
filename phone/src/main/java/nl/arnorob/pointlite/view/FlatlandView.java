package nl.arnorob.pointlite.view;

import java.util.Arrays;

import nl.arnorob.pointlite.CompassManager;
import nl.arnorob.pointlite.Point;
import nl.arnorob.pointlite.TrackManager;
import nl.arnorob.pointlite.model.Trackable;
import nl.arnorob.pointlite.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class FlatlandView extends View {
	private final TrackManager tm;
	private final CompassManager cm;
	public final static float KM_TO_MILES = 0.621371192237f;
	public static final boolean MODE_MILES = true;
	public static final boolean MODE_KM = false;
	private final int[] ringDistances = { 10, 100, 1000, 10000, 100000, 1000000, 10000000 };

	// private int[] ringDistancesMiles = { 10, 100, 1000, 10000, 100000, 1000000, 10000000 };
	// private int[] ringDistancesMiles = { 50 ft, 500 ft/0.1 mile, 1 mile, 10, 100, 1000, 10000 mile };

	private static final int min = 6;
	private static final int max = 25;
	private final static int MID_MULTIPLY = 50;
	private double unit;
	private float heading;
	private boolean useMiles = false;

	private static int COLOR_BACK = 0xff111111;
	private static int COLOR_HIGHLITE = 0xff1111ff;
	private static int COLOR_ACCURACY = COLOR_HIGHLITE;
	private final Paint accuracyPaint;
	private final Paint shadowPaint;
	private final Paint arrowPaint;
	private final Paint arrowTextPaint;
	private final Paint ringPaint;
	private final Paint erasePaint;
	private final Paint ringTextPaint;
	private final Paint accuracyTextPaint;
	private final Paint highLitePaint;
	private final Path northArrow;
	private final Path arrowPath;
	private final Matrix arrowMatrix;
	private Paint noLocationTextPaint;
	private int midx;
	private int midy;
	private ArrowDrawingData[] drawData;

	private Drawable satellite;
	private Paint gpsFixTextPaint;
	private Paint gpsNoFixTextPaint;

	public FlatlandView(Context context, TrackManager tm, CompassManager cm) {
		super(context);
		setClickable(true);
		setEnabled(true);
		this.tm = tm;
		this.cm = cm;
		cm.requestInvalidates(this);
		tm.requestInvalidates(this);

		satellite = getResources().getDrawable(R.drawable.satellite);
		accuracyPaint = new Paint();
		accuracyPaint.setAntiAlias(true);
		accuracyPaint.setColor(COLOR_ACCURACY);
		accuracyPaint.setStyle(Style.FILL);
		accuracyPaint.setAlpha(80);

		shadowPaint = new Paint();
		shadowPaint.setAntiAlias(true);
		shadowPaint.setColor(Color.BLACK);
		shadowPaint.setStyle(Style.FILL);
		shadowPaint.setAlpha(100);

		arrowPaint = new Paint();
		arrowPaint.setAntiAlias(true);
		arrowPaint.setColor(Color.BLACK);
		arrowPaint.setStyle(Style.FILL);
		arrowPaint.setAlpha(192);

		ringPaint = new Paint();
		ringPaint.setAntiAlias(true);
		ringPaint.setColor(Color.LTGRAY);
		ringPaint.setAlpha(255);
		ringPaint.setStyle(Style.STROKE);

		erasePaint = new Paint();
		erasePaint.setColor(COLOR_BACK);
		erasePaint.setAlpha(255);
		erasePaint.setStyle(Style.FILL);

		arrowTextPaint = new Paint();
		arrowTextPaint.setAntiAlias(true);
		arrowTextPaint.setTextSize(10);
		arrowTextPaint.setColor(Color.WHITE);
		arrowTextPaint.setStyle(Style.FILL);
		arrowTextPaint.setAlpha(255);

		noLocationTextPaint = new Paint();
		noLocationTextPaint.setAntiAlias(true);
		noLocationTextPaint.setTextSize(20);
		noLocationTextPaint.setColor(Color.WHITE);
		noLocationTextPaint.setStyle(Style.FILL);
		noLocationTextPaint.setAlpha(255);

		ringTextPaint = new Paint();
		ringTextPaint.setTextSize(10);
		ringTextPaint.setAntiAlias(true);
		ringTextPaint.setColor(Color.WHITE);
		ringTextPaint.setStyle(Style.FILL);
		ringTextPaint.setAlpha(255);

		accuracyTextPaint = new Paint();
		accuracyTextPaint.setTextSize(10);
		accuracyTextPaint.setAntiAlias(true);
		accuracyTextPaint.setColor(COLOR_ACCURACY);
		accuracyTextPaint.setStyle(Style.FILL);
		accuracyTextPaint.setAlpha(255);

		gpsFixTextPaint = new Paint();
		gpsFixTextPaint.setTextSize(14);
		gpsFixTextPaint.setAntiAlias(true);
		gpsFixTextPaint.setColor(COLOR_ACCURACY);
		gpsFixTextPaint.setStyle(Style.FILL);
		gpsFixTextPaint.setAlpha(255);

		gpsNoFixTextPaint = new Paint(gpsFixTextPaint);
		gpsNoFixTextPaint.setColor(Color.RED);

		highLitePaint = new Paint();
		highLitePaint.setAntiAlias(true);
		highLitePaint.setColor(COLOR_HIGHLITE);
		highLitePaint.setAlpha(255);
		highLitePaint.setStyle(Style.FILL);

		northArrow = new Path();
		northArrow.moveTo(0, 4);
		northArrow.lineTo(-7, 0);
		northArrow.lineTo(0, 10);
		northArrow.lineTo(7, 0);
		northArrow.lineTo(0, 4);

		arrowPath = new Path();
		arrowPath.moveTo(0, 6);
		arrowPath.lineTo(-3, 6);
		arrowPath.lineTo(-10, 20);
		arrowPath.lineTo(-15, 20);
		arrowPath.lineTo(0, 25);
		arrowPath.lineTo(15, 20);
		arrowPath.lineTo(10, 20);
		arrowPath.lineTo(3, 6);
		arrowPath.lineTo(0, 6);
		arrowPath.close();

		arrowMatrix = new Matrix();
	}

	@Override
	public boolean performClick() {
		try {
			//if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Intent intent = new Intent(getContext(), nl.arnorob.pointlite.mapmark.MapMarkList.class);
				Location loc = tm.getLocation();
				intent.putExtra("latitude", (float) loc.getLatitude());
				intent.putExtra("longitude",(float) loc.getLongitude());
				getContext().startActivity(intent);
				return super.performClick();
			//}
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "", t);
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		try {
//			if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                Intent intent = new Intent(getContext(), nl.arnorob.pointlite.mapmark.MapMarkList.class);
//                Location loc = tm.getLocation();
//                intent.putExtra("latitude", (float) loc.getLatitude());
//                intent.putExtra("longitude",(float) loc.getLongitude());
//                getContext().startActivity(intent);
//				return true;
//			}
//		} catch (Throwable t) {
//			Log.e(getClass().getSimpleName(), "", t);
//		}
//		return false;
		return super.onTouchEvent(event);
	}


	public synchronized void onDraw(Canvas canvas) {
		// Log.i("FlatLandView", "redraw");
		canvas.drawColor(COLOR_BACK);

		midx = getWidth() / 2;
		midy = getHeight() / 2;

//		if (tm.isUsingGPS()) {
//			satellite.setBounds(5, midx + midy - 30, 5 + 30, midx + midy);
//			satellite.draw(canvas);
//			Paint p = tm.hasGPSLock() ? gpsFixTextPaint : gpsNoFixTextPaint;
//			canvas.drawText("" + tm.getGPSSatellites(), 40, midx + midy, p);
//		}

		final double longestArrow = (tm.getMostDistant() == null) ? 20000000 : tm.getMostDistant().getDistance();
		int maxRingToDraw = ringDistances[ringDistances.length - 1];
		for (int i = ringDistances.length - 1; i > 1; i--) {// always show the first 3 rings (0,1 and 2)
			if (longestArrow > ringDistances[i]) {
				break;
			} else {
				maxRingToDraw = ringDistances[i];
			}
		}

		unit = Math.min(midx, midy) / Math.log10(maxRingToDraw * MID_MULTIPLY);
		canvas.save();
		{// save
			canvas.translate(midx, midy);
			canvas.drawCircle(0, 0, 2, highLitePaint);

			float accuracy = 200;
			Location loc = tm.getLocation();
			if (loc != null && loc.hasAccuracy()) {
				accuracy = tm.getLocation().getAccuracy();
			}
			for (int ringDistance : ringDistances) {
				if (ringDistance <= maxRingToDraw) {
					drawRing(canvas, ringDistance);
				}
			}

			heading = cm.getHeading() + 180;

			// draw arrows below accuracy circle
			canvas.save();
			canvas.rotate(-heading);
			if (loc != null) {
				Trackable[] tracks = tm.getTrackables();
				drawData = new ArrowDrawingData[tracks.length];
				// no location so do not draw arrows
				for (int i = 0; i < tracks.length; i++) {
					Trackable t = tracks[i];
					if (t.getDistance() <= accuracy) {
						drawData[i] = drawArrow(canvas, t);
					}
				}
				canvas.restore();
				drawAccuracy(canvas, accuracy);

				// draw arrows above accuracy circle
				canvas.save();
				canvas.rotate(-heading);
				for (int i = 0; i < tracks.length; i++) {
					Trackable t = tracks[i];
					if (t.getDistance() > accuracy) {
						drawData[i] = drawArrow(canvas, t);
					}
				}
			}
			canvas.drawPath(northArrow, highLitePaint);
			canvas.restore();
			if (loc == null) {
				drawNoLocationText(canvas);// relative to center
			}
		}
		canvas.restore();
		// canvas.drawText("angle:"+, start, end, x, y, paint)
	}

	private void drawNoLocationText(Canvas canvas) {
		{
			String text = getResources().getString(R.string.no_location);
			final float textLength = noLocationTextPaint.measureText(text);
			canvas.drawText(text, -textLength / 2, 30, noLocationTextPaint);
		}
		{
			String text2 = getResources().getString(R.string.check_loc_settings);
			final float textLength2 = ringTextPaint.measureText(text2);
			canvas.drawText(text2, -textLength2 / 2, 50, ringTextPaint);
		}
	}

	private void drawAccuracy(Canvas canvas, float accuracy) {
		if (accuracy < 0.1)
			return;// no accuracy available
		float dist = scale(accuracy);
		canvas.drawCircle(0, 0, dist, accuracyPaint);
		drawDistance(canvas, accuracyTextPaint, (int) accuracy, dist, 270, false);
	}

	private void drawRing(Canvas canvas, int ringDistance) {
		float size = scale(ringDistance);
		canvas.drawCircle(0, 0, size, ringPaint);
		drawDistance(canvas, ringTextPaint, ringDistance, size, 0, true);
	}

	private void drawDistance(Canvas canvas, Paint textPaint, final int distanceMeters, final float distancepixels, float angle, boolean clearBack) {
		canvas.save();
		canvas.rotate(angle);
		final String distanceString = getDistanceString(distanceMeters, useMiles);
		final float textLength = textPaint.measureText(distanceString);
		final int right = (int) (textLength / 2) + 4;
		final int left = -right;
		if (clearBack) {
			canvas.drawRect(left, distancepixels - 6, right, distancepixels + 4, erasePaint);
			canvas.drawRect(left, -distancepixels - 4, right, -distancepixels + 6, erasePaint);
		}
		canvas.drawText(distanceString, left + 4, distancepixels + 3, textPaint);
		canvas.drawText(distanceString, left + 4, -distancepixels + 5, textPaint);
		canvas.restore();
	}

	private float scale(double distance) {
		return (float) (Math.log10(distance * MID_MULTIPLY) * unit);
	}

	public static String getDistanceString(int distance, boolean useMiles) {
		if (useMiles) {
			if (distance < 1000)
				return distance + " yd";
			else
				return distance / 1000 + " mile";
		} else {
			if (distance < 10000)
				return distance + " m";
			else
				return distance / 1000 + " km";
		}
	}

	// private float unit(float distance) {
	// if (useMiles) {
	// return distance * KM_TO_MILES;
	// }
	// return distance;
	// }

	private ArrowDrawingData drawArrow(Canvas canvas, Trackable trackable) {
		canvas.save();
		final double distance = Math.max(10, trackable.getDistance());
		final float arrowPixelLength = scale(distance);
		final float scale = arrowPixelLength / max;

		final Path arrow = new Path();

		arrowMatrix.setScale(1, scale);
		// Transform the points in this path by matrix, and write the answer into dst. If dst is null, then the the original path is modified.
		arrowPath.transform(arrowMatrix, arrow);
		float bearing = trackable.getBearing();

		// angle in degrees from 0-540: 0 is down and up is counter clockwise
		final float absolutArrowDirection = heading - bearing;

		// Log.i("angle for " + trackable.name, "" + absolutArrowDirection);

		canvas.rotate(bearing);

		canvas.save();
		// make shadow on same side for all arrows: calculate x and y shift
		final double bRad = Math.toRadians(absolutArrowDirection);
		final double y = Math.sin(bRad);
		final double x = Math.cos(bRad);
		canvas.translate((int) (+10 * x), (int) (+10 * y));
		canvas.drawPath(arrow, shadowPaint);
		canvas.restore();

		// draw arrow
		LinearGradient gradient = new LinearGradient(0, scale * min, 0, arrowPixelLength, Color.BLACK, trackable.color, Shader.TileMode.REPEAT);
		arrowPaint.setShader(gradient);
		canvas.drawPath(arrow, arrowPaint);

		// draw text
		arrowTextPaint.setTextScaleX(1);
		float textLength = arrowTextPaint.measureText(trackable.name);
		arrowTextPaint.setTextScaleX((scale * (max - min) - 15) / textLength);

		float absolutArrowDirection2 = absolutArrowDirection;
		while (absolutArrowDirection2 >= 360) {
			absolutArrowDirection2 -= 360;
		}

		while (absolutArrowDirection2 < 0) {
			absolutArrowDirection2 += 360;
		}

		if (absolutArrowDirection2 > 180) {// && absolutArrowDirection2 > 0) {
			canvas.rotate(-90);
			canvas.drawText(trackable.name, 0, trackable.name.length(), -arrowPixelLength + 10, 4, arrowTextPaint);
		} else {
			canvas.rotate(90);
			canvas.drawText(trackable.name, 0, trackable.name.length(), scale * min + 5, 4, arrowTextPaint);
		}
		canvas.restore();
		return new ArrowDrawingData(absolutArrowDirection2, arrowPixelLength, trackable);
	}
}
