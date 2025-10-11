package nl.arnorob.pointlite;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CompassManagerDepricated extends ViewUpdater implements SensorListener , CompassManager{

	private float headingAngle = 0; // North
	public float pitchAngle;
	public float rollAngle;
	private SensorManager sensorManager;

	public CompassManagerDepricated(Activity context) {
		super(context);
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		registerListener();
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
						updateHeading(heading);
					}
				} catch (InterruptedException e) {
					Log.e("thread", "", e);
				}
			}
		};
		t.start();
	}

	private void registerListener() {
		sensorManager.registerListener(this, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onSensorChanged(int arg0, float[] values) {
		try {
			updateHeading(values[0]);
		} catch (Throwable t) {
			Log.e("Sensor error", "" + this.getClass().getSimpleName(), t);
		}
	}

	private void updateHeading(float headingAngle) {
		if (Math.abs(this.headingAngle - headingAngle) > 4) {
			this.headingAngle = headingAngle;
			super.invalidateViews();
		}
	}

	public void onAccuracyChanged(int arg0, int arg1) {

	}

	public void resume() {
		registerListener();
	}

	public void pause() {
		sensorManager.unregisterListener(this);
	}

	public float getHeading() {
		return headingAngle;
	}
}