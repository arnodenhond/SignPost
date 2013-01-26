package nl.arnorob.point;

import java.util.Arrays;

import nl.arnorob.point.model.Trackable;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

public class TrackManager extends ViewUpdater {

	private Trackable[] trackables = new Trackable[0];
	private Location location;
	
	public TrackManager(Activity context) {
		super(context);
	}

	public Trackable[] getTrackables() {
		return trackables;
	}

	/**
	 * Applies a new set of sorted trackables. Each trackable is updated to
	 * current location. Invalidates all subscribed views.
	 * 
	 * @param trackables
	 *            if null, an empty (but non-null) array of trackables is
	 *            applied.
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
		return true;
		// return GPS_PROVIDER.equals(currentProvider);
	}

	public boolean hasGPSLock() {
		return true;
		// return gpsstatus == LocationProvider.AVAILABLE ||
		// receivedGpsLocation;
	}

	/**
	 * Updates bearing and distance to new location on all trackables.
	 * Invalidates all subscribed views.
	 * 
	 * @param newLocation
	 *            if null, ignore and keep old location
	 */

	public void onLocationChanged(final Location newLocation) {
		this.location = getLocation();
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
		Location loc = new Location((String) null);
		SharedPreferences prefs = a.getSharedPreferences("MYLOCATION", Context.MODE_PRIVATE);
		loc.setLatitude(prefs.getFloat("LAT", -17.531f));
		loc.setLongitude(prefs.getFloat("LON",-149.830f));
		loc.setAccuracy(1f);
		return loc;
		// return location;
	}

}