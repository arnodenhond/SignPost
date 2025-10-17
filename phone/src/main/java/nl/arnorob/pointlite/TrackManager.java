package nl.arnorob.pointlite;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;

import nl.arnorob.pointlite.model.Trackable;

public class TrackManager extends ViewUpdater {

    private Trackable[] trackables = new Trackable[0];
    private Location location;
    private FusedLocationProviderClient fusedLocationClient;

    public TrackManager(Activity context) {
        super(context);
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        } catch (SecurityException e) {
            Log.e("TrackManager", "Permission issue, have you granted permissions?", e);
        }
    }

    public Trackable[] getTrackables() {
        return trackables;
    }

    public void setTrackables(Trackable[] trackables) {
        if (trackables == null) {
            trackables = new Trackable[0];
        }
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
        return location != null;
    }


    public boolean hasGPSLock() {
        return location != null && location.hasAccuracy() && location.getAccuracy() < 25;
    }

    public int getGPSSatellites() {
        if (location != null) {
            Bundle extras = location.getExtras();
            if (extras != null) {
                return extras.getInt("satellites", 0);
            }
        }
        return 0;
    }

    public void onLocationChanged(final Location newLocation) {
        if (newLocation == null)
            return;
        this.location = newLocation;
        for (Trackable trackable : trackables) {
            trackable.update(location);
        }
        Arrays.sort(trackables);
        invalidateViews();
    }

    public Location getLocation() {
        return location;
    }

    public void pause() {
    }

    public void resume() {
         if (fusedLocationClient == null) return;
         try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        onLocationChanged(location);
                    }
                }
            });

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                       onLocationChanged(location);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(getClass().getSimpleName(), "Location permission not granted", e);
        }
    }
}
