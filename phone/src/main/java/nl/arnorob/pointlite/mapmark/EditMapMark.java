package nl.arnorob.pointlite.mapmark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.flask.colorpicker.ColorPickerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import nl.arnorob.pointlite.R;
import nl.arnorob.pointlite.db.DBAdapter;
import nl.arnorob.pointlite.proximity.AlertSetter;

public class EditMapMark extends FragmentActivity implements OnMapReadyCallback {

	private static final int PROXIMITY = 0;
	protected static final int UPGRADE = 1;
	DBAdapter db;
	ViewGroup proximity;
	int color;
	boolean enabled;

	private GoogleMap mMap;
	private Marker mMarker;
	private LatLng mLatLng;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.mapmark);
		SharedPreferences prefs = getSharedPreferences("mapmark", MODE_PRIVATE);
		final long id = prefs.getLong("mapmark", -1);
		db = new DBAdapter(EditMapMark.this);
		db.open();
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(this);
		}

		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		proximity = (ViewGroup) li.inflate(R.layout.proximity, findViewById(android.R.id.content), false);
		final EditText title = findViewById(R.id.Title);
		final CheckBox proxenter = proximity.findViewById(R.id.proxenter);
		final CheckBox proxexit = proximity.findViewById(R.id.proxexit);
		final EditText proxradius = proximity.findViewById(R.id.proxradius);
		Button colorButton = findViewById(R.id.color);
		color = Color.RED;
		enabled = true;

		if (id != -1) {
			Cursor cursor = db.getMapMark(id);
			cursor.moveToFirst();
			title.setText(cursor.getString(DBAdapter.NAME_COLUMN));
			proxenter.setChecked(cursor.getInt(DBAdapter.PROXENTER_COLUMN) == DBAdapter.ON);
			proxexit.setChecked(cursor.getInt(DBAdapter.PROXEXIT_COLUMN) == DBAdapter.ON);
			proxradius.setText(String.format(Locale.ROOT, "%d", cursor.getInt(DBAdapter.PROXRADIUS_COLUMN)));
			color = cursor.getInt(DBAdapter.COLOR_COLUMN);
			enabled = cursor.getInt(DBAdapter.POINTERENABLED_COLUMN) == DBAdapter.ON;
			double latitude = cursor.getDouble(DBAdapter.LATITUDE_COLUMN);
			double longitude = cursor.getDouble(DBAdapter.LONGITUDE_COLUMN);
			mLatLng = new LatLng(latitude, longitude);
			cursor.close();
		}

		if (icicle != null) {
			color = icicle.getInt("color");
			if (icicle.containsKey("latitude")) {
				double latitude = icicle.getDouble("latitude");
				double longitude = icicle.getDouble("longitude");
				mLatLng = new LatLng(latitude, longitude);
			}
		}

		Button save = findViewById(R.id.Save);
		save.setOnClickListener(v -> {
			if (mLatLng == null) {
				Toast.makeText(EditMapMark.this, R.string.place_marker, Toast.LENGTH_SHORT).show();
				return;
			}
			double dlat = mLatLng.latitude;
				double dlon = mLatLng.longitude;
				String titlestring = title.getText().toString();

				String sproxradius = proxradius.getText().toString();
				if (sproxradius.isEmpty())
					sproxradius = "0";
				if (id == -1) {
					long newid = db.insertMapMark(titlestring, dlat, dlon, color, proxenter.isChecked(), proxexit.isChecked(), Integer.parseInt(sproxradius), DBAdapter.MAPMARK);
					if (enabled)
						AlertSetter.setAlert(EditMapMark.this, newid, true);
				} else {
					db.updateMapMark(id, titlestring, dlat, dlon, color, proxenter.isChecked(), proxexit.isChecked(), Integer.parseInt(sproxradius));
					if (enabled) {
						AlertSetter.setAlert(EditMapMark.this, id, false);
						AlertSetter.setAlert(EditMapMark.this, id, true);
					}
				}

				Toast.makeText(EditMapMark.this, R.string.mapmark_saved, Toast.LENGTH_SHORT).show();
				finish();
			});

		Button proxalert = findViewById(R.id.proxalerts);
		proxalert.setOnClickListener(v -> proximityButtonClicked());

		colorButton.setOnClickListener(v -> com.flask.colorpicker.builder.ColorPickerDialogBuilder
				.with(EditMapMark.this)
				.setTitle("Choose color")
				.initialColor(color)
				.wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
				.density(12)
				.setOnColorSelectedListener(selectedColor -> {
					//
				})
				.setPositiveButton("ok", (dialog, selectedColor, allColors) -> color = selectedColor)
				.setNegativeButton("cancel", (dialog, which) -> {
				})
				.build()
				.show());
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		if (mLatLng != null) {
			mMarker = mMap.addMarker(new MarkerOptions().position(mLatLng));
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15));
		}

		mMap.setOnMapClickListener(point -> {
			if (mMarker == null) {
				mMarker = mMap.addMarker(new MarkerOptions().position(point));
			} else {
				mMarker.setPosition(point);
			}
			mLatLng = point;
		});
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("color", color);
		if (mLatLng != null) {
			outState.putDouble("latitude", mLatLng.latitude);
			outState.putDouble("longitude", mLatLng.longitude);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROXIMITY:
			return new AlertDialog.Builder(this).setTitle(R.string.proxalert).setView(proximity).setPositiveButton(R.string.save, (dialog, which) -> dismissDialog(0)).create();
		case UPGRADE:
			return new AlertDialog.Builder(this).setMessage(R.string.upgradetext).setPositiveButton(R.string.upgradebutton, (dialog, which) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:nl.arnorob.pointlite")))).create();
		default:
			throw new IllegalArgumentException("not supported:" + id);
		}
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	protected void proximityButtonClicked() {
		showDialog(PROXIMITY);
	}
}
