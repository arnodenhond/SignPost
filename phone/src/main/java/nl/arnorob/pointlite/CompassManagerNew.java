package nl.arnorob.pointlite;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CompassManagerNew extends ViewUpdater implements SensorEventListener, CompassManager {

	private float headingAngle = 180; // North
	public float pitchAngle;
	public float rollAngle;
	private SensorManager sensorManager;

	public CompassManagerNew(Activity context) {
		super(context);
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		registerSensor();
	}

	private void registerSensor() {
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void simulate() {
		Thread t = new Thread() {

			Random r = new Random();

			public void run() {
				try {
					float heading = 180;
					Thread.sleep(3000);
					while (true) {
						Thread.sleep(50);
						heading += 5;
						if (heading > 360) {
							heading += 360;
						}
						updateHeading(heading, null);
					}
				} catch (InterruptedException e) {
					Log.e("thread", "", e);
				}
			}
		};
		t.start();
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public void onSensorChanged(SensorEvent event) {
		try {

			// rollAngle = event.values[SensorManager.DATA_X];
			// pitchAngle = event.values[SensorManager.DATA_Y];
			final float headingAngle2 = event.values[0];
			// final float headingAngle2=event.values[SensorManager.RAW_DATA_X];

			updateHeading(headingAngle2, event);
		} catch (Throwable t) {
			Log.e("Sensor error", "" + this.getClass().getSimpleName(), t);
		}
	}

	private void updateHeading(final float headingAngle, SensorEvent event) {
		if (headingAngle == 0f)
			return; // sometimes an exact 0 value is given when switching back to the point activity (compass start in log)
		//gives dirty redraw
		if (Math.abs(this.headingAngle - headingAngle) > 2) {
			this.headingAngle = headingAngle;
			//Log.i(getClass().getSimpleName(), "heading:" + event.sensor.getName() + " " + headingAngle);
			super.invalidateViews();
		}
	}

	public void onAccuracyChanged(int arg0, int arg1) {

	}

	public void onSensorChanged(int arg0, float[] arg1) {

	}

	public float getHeading() {
		return headingAngle;
	}

	public void pause() {
		sensorManager.unregisterListener(this);
	}

	public void resume() {
		registerSensor();
	}

}