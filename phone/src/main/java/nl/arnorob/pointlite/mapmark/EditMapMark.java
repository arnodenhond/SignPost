package nl.arnorob.pointlite.mapmark;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flask.colorpicker.ColorPickerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import nl.arnorob.pointlite.R;
import nl.arnorob.pointlite.db.DBAdapter;

public class EditMapMark extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private DBAdapter db;
    private long mmid;
    private EditText nameEditText;
    private Button colorButton;
    private double lat;
    private double lon;
    private int color;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapmark);

        db = new DBAdapter(this);
        db.open();

        SharedPreferences prefs = getSharedPreferences("mapmark", MODE_PRIVATE);
        mmid = prefs.getLong("mapmark", -1);

        nameEditText = findViewById(R.id.Title);
        colorButton = findViewById(R.id.color);

        if (mmid != -1) {
            Cursor cursor = db.getMapMark(mmid);
            if (cursor.moveToFirst()) {
                name = cursor.getString(DBAdapter.NAME_COLUMN);
                lat = cursor.getDouble(DBAdapter.LATITUDE_COLUMN);
                lon = cursor.getDouble(DBAdapter.LONGITUDE_COLUMN);
                color = cursor.getInt(DBAdapter.COLOR_COLUMN);
            }
            cursor.close();
        } else {
            name = "New Map Mark";
            color = Color.RED;
        }

        nameEditText.setText(name);
        colorButton.setBackgroundColor(color);

        colorButton.setOnClickListener(v -> com.flask.colorpicker.builder.ColorPickerDialogBuilder
                .with(EditMapMark.this)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {
                    //
                })
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                    this.color = selectedColor;
                    colorButton.setBackgroundColor(this.color);
                })
                .setNegativeButton("cancel", (dialog, which) -> {
                })
                .build()
                .show());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        mMap.setOnMapClickListener(latLng -> {
            lat = latLng.latitude;
            lon = latLng.longitude;
            updateMap();
        });
        updateMap();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    private void updateMap() {
        if (mMap == null) return;
        mMap.clear();
        LatLng location = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(location).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void save() {
        name = nameEditText.getText().toString();
        if (mmid != -1) {
            db.updateMapMark(mmid, name, lat, lon, color, false, false, 0);
        } else {
            mmid = db.insertMapMark(name, lat, lon, color, false, false, 0, DBAdapter.SPOT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_map_mark_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            save();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
