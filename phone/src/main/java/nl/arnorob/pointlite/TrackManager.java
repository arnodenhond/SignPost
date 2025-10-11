package nl.arnorob.pointlite;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

import java.util.Arrays;

import nl.arnorob.pointlite.model.Trackable;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class TrackManager extends ViewUpdater implements LocationListener, GpsStatus.Listener {

	private static final float THRESHOLD_DISTANCE = 5;
	private static final long THRESHOLD_TIME = 5 * 1000;

	private Trackable[] trackables = new Trackable[0];
	private Location location;
	private LocationManager locationManager;
	private String currentProvider;
	private final Criteria criteria;
	private int gpsstatus;
	private boolean receivedGpsLocation = false;
	private LocationListener networkListener;
	private int satellites;
	private GpsStatus gpsStatusObject;

	public TrackManager(Activity context) {
		super(context);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		refreshLocationUpdater();
	}

	/**
	 * Tries to get the best available location provider. If none, keep old provider. If a new provider is found, the old is unsubscribed and the new is subscribed. Tries to get location from new provider. Trackables are updated to current location and views are invalidated
	 */
	private void refreshLocationUpdater() {
		String newProvider = locationManager.getBestProvider(criteria, true);
		if (newProvider == null || newProvider.equals("")) {
			return;
		}
		if (!newProvider.equals(currentProvider)) {
			locationManager.removeUpdates(this);
			locationManager.requestLocationUpdates(newProvider, THRESHOLD_TIME, THRESHOLD_DISTANCE, this);
			currentProvider = newProvider;
		}
	}

	public Trackable[] getTrackables() {
		return trackables;
	}

	/**
	 * Applies a new set of sorted trackables. Each trackable is updated to current location. Invalidates all subscribed views.
	 * 
	 * @param trackables
	 *            if null, an empty (but non-null) array of trackables is applied.
	 */
	public void setTrackables(Trackable[] trackables) {
		if (trackables == null) {
			trackables = new Trackable[0];
		}// TODO check if none of the elements are null
		this.trackables = trackables;
		onLocationChanged(location);
	}

	public Trackable getMostDistant() {
		if (trackables.length > 0)
			return trackables[0];
		else
			return null;
	}

	public boolean isUsingGPS() {
		return GPS_PROVIDER.equals(currentProvider);
	}

	public boolean hasGPSLock() {
		return gpsstatus == LocationProvider.AVAILABLE || receivedGpsLocation;
	}

	/**
	 * Updates bearing and distance to new location on all trackables. Invalidates all subscribed views.
	 * 
	 * @param newLocation
	 *            if null, ignore and keep old location
	 */

	public void onLocationChanged(final Location newLocation) {
		if (newLocation == null)
			return;
		this.location = newLocation;
		receivedGpsLocation=true;
		// clear accuracy before sending out
		final Location l = new Location(location);
		l.removeAccuracy();
		for (Trackable trackable : trackables) {
			trackable.update(l);
		}
		Arrays.sort(trackables);
		invalidateViews();
	}

	public Location getLocation() {
		return location;
	}

	public void onProviderDisabled(String provider) {
		refreshLocationUpdater();
	}

	public void onProviderEnabled(String provider) {
		refreshLocationUpdater();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		refreshLocationUpdater();
		Log.i(getClass().getName(), "state of " + provider + ":" + getLocationProviderStatusString(status));
		if (currentProvider.equals(GPS_PROVIDER)) {
			satellites = extras.getInt("satellites");
			Log.i(getClass().getName(), "satellites:" + satellites);
			gpsstatus = status;
			invalidateViews();
		}
	}

	private String getExtrasToString(Bundle extras) {
		StringBuilder buf = new StringBuilder(" extras: ");
		for (String key : extras.keySet()) {
			buf.append(key);
			buf.append(": ");
			buf.append("(" + extras.get(key).getClass().getSimpleName() + ") ");
			buf.append(extras.get(key).toString());
			buf.append(" | ");
		}
		return buf.toString();
	}

	private String getLocationProviderStatusString(int status) {
		switch (status) {
		case LocationProvider.AVAILABLE:
			return "AVAILABLE";
		case LocationProvider.OUT_OF_SERVICE:
			return "OUT_OF_SERVICE";
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			return "TEMPORARILY_UNAVAILABLE";
		default:
			return "unknown status" + status;
		}
	}

	public void pause() {
		locationManager.removeUpdates(this);
		locationManager.removeUpdates(networkListener);
		locationManager.removeGpsStatusListener(this);
		currentProvider = null;
	}

	public void resume() {
		refreshLocationUpdater();
		refreshNetworkListener();
		locationManager.addGpsStatusListener(this);
	}

	private void refreshNetworkListener() {
		newNetworkLocation(locationManager.getLastKnownLocation(NETWORK_PROVIDER));
		if (networkListener == null) {
			networkListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					newNetworkLocation(location);
				}

				public void onProviderDisabled(String provider) {
				}

				public void onProviderEnabled(String provider) {
				}

				public void onStatusChanged(String provider, int status, Bundle extras) {
				}
			};
		}
		locationManager.requestLocationUpdates(NETWORK_PROVIDER, THRESHOLD_TIME, THRESHOLD_DISTANCE, networkListener);
	}

	private void newNetworkLocation(Location location) {
		// use network location if gps fails
		if (gpsstatus != LocationProvider.AVAILABLE) {
			onLocationChanged(location);
		}
	}

	public int getGPSSatellites() {
		return satellites;
	}

	public void onGpsStatusChanged(int event) {
		processGPSStatus();
	}

	private void processGPSStatus() {
		gpsStatusObject = locationManager.getGpsStatus(null);
		// if a lock is already there than onStatusChanged will not be called!!
		// TODO need better way to count and get status!!
		int count = 0;
		for (GpsSatellite sat : gpsStatusObject.getSatellites()) {
			count++;
		}
		if (count != satellites) {
			satellites = count;
			// allow redraw of nr of satellites
			invalidateViews();
		}

//		if (satellites > 3) {
//			// need 4 sats for locating
//			if (gpsstatus != LocationProvider.AVAILABLE) {
//				// set it once when gps comes available
//				// (otherwise it will show the network location until a move is done and the location is updated)
//				onLocationChanged(locationManager.getLastKnownLocation(GPS_PROVIDER));
//			}
//			gpsstatus = LocationProvider.AVAILABLE;
//		}
		// Log.i(getClass().getName(), "status:" + getLocationProviderStatusString(gpsstatus) + " sats:" + satellites);
	}
}