package nl.arnorob.pointlite.mapmark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.wear.widget.SwipeDismissFrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import nl.arnorob.pointlite.R;


public class WearLocationPicker extends Activity implements OnMapReadyCallback {

    MapFragment mapFragment;
    double lat;
    double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// Set the layout. It only contains a SupportMapFragment and a DismissOverlay.
        setContentView(R.layout.activity_wear_location_picker);

        final SwipeDismissFrameLayout mapFrameLayout = (SwipeDismissFrameLayout) findViewById(
                R.id.map_container);
        mapFrameLayout.addCallback(new SwipeDismissFrameLayout.Callback() {
            @Override
            public void onDismissed(SwipeDismissFrameLayout layout) {
                onBackPressed();
            }
        });

        lat = getIntent().getDoubleExtra("lat",0);
        lon = getIntent().getDoubleExtra("lon",0);


        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng here = new LatLng(lat,lon);
        // Add a marker with a title that is shown in its info window.
        googleMap.addMarker(new MarkerOptions().position(here));
        Intent result = new Intent();
        result.putExtra("lat",lat);
        result.putExtra("lon",lon);
        setResult(RESULT_OK,result);

        // Move the camera to show the marker.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 10));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                googleMap.clear();
                Intent newresult = new Intent();
                newresult.putExtra("lat",latLng.latitude);
                newresult.putExtra("lon",latLng.longitude);
                setResult(RESULT_OK,newresult);
                googleMap.addMarker(new MarkerOptions().position(latLng));
            }
        });
    }




}