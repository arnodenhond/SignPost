package nl.arnorob.point.mapmark;

import nl.arnorob.point.db.DBAdapter;
import nl.arnorob.point.proximity.AlertSetter;
import nl.arnorob.pointpro.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

//debug maps key ==0ajyeMpdys7HJEfvfLk5uzCCK85VKP9HprlZFdA==

//0ajyeMpdys7FjdqnDTMflFLzIDyf9EI4zOs1KQA
public class EditMapMark extends MapActivity implements ColorPickerDialog.OnColorChangedListener {

	private static final int PROXIMITY = 0;
	protected static final int UPGRADE = 1;
	DBAdapter db;
	ViewGroup proximity;
	int color;
	boolean enabled;
	MapMarkOverlay mapMarkOverlay;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.mapmark);
		SharedPreferences prefs = getSharedPreferences("mapmark", MODE_PRIVATE);
		final long id = prefs.getLong("mapmark", -1);
		db = new DBAdapter(EditMapMark.this);
		db.open();
		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapMarkOverlay = new MapMarkOverlay(getResources().getDrawable(R.drawable.icon_no_shadow));
		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		proximity = (ViewGroup) li.inflate(R.layout.proximity, null);
		final EditText title = (EditText) findViewById(R.id.Title);
		final CheckBox proxenter = (CheckBox) proximity.findViewById(R.id.proxenter);
		final CheckBox proxexit = (CheckBox) proximity.findViewById(R.id.proxexit);
		final EditText proxradius = (EditText) proximity.findViewById(R.id.proxradius);
		Button colorButton = (Button) findViewById(R.id.color);
		color = Color.RED;
		enabled = true;
		if (id != -1) {
			Cursor cursor = db.getMapMark(id);
			cursor.moveToFirst();
			title.setText(cursor.getString(DBAdapter.NAME_COLUMN));
			proxenter.setChecked(cursor.getInt(DBAdapter.PROXENTER_COLUMN) == DBAdapter.ON);
			proxexit.setChecked(cursor.getInt(DBAdapter.PROXEXIT_COLUMN) == DBAdapter.ON);
			proxradius.setText(Integer.toString(cursor.getInt(DBAdapter.PROXRADIUS_COLUMN)));
			color = cursor.getInt(DBAdapter.COLOR_COLUMN);
			enabled = cursor.getInt(DBAdapter.POINTERENABLED_COLUMN) == DBAdapter.ON;
			int latitude = (int) (cursor.getDouble(DBAdapter.LATITUDE_COLUMN) * 1E6);
			int longitude = (int) (cursor.getDouble(DBAdapter.LONGITUDE_COLUMN) * 1E6);
			GeoPoint geopoint = new GeoPoint(latitude, longitude);
			mapMarkOverlay = new MapMarkOverlay(getResources().getDrawable(R.drawable.icon_no_shadow), geopoint);
			MapController mc = mapView.getController();
			mc.setCenter(geopoint);
			cursor.close();
		}

		if (icicle != null) {
			color = icicle.getInt("color");
			int latitude = icicle.getInt("latitude");
			int longitude = icicle.getInt("longitude");
			GeoPoint geopoint = new GeoPoint(latitude, longitude);
			mapMarkOverlay = new MapMarkOverlay(getResources().getDrawable(R.drawable.icon_no_shadow), geopoint);
			MapController mc = mapView.getController();
			mc.setCenter(geopoint);
		}
		ColorPickerDialog cpd = new ColorPickerDialog(this, this, color);

		mapView.getOverlays().add(mapMarkOverlay);

		final MapMarkOverlay fmapMarkOverlay = mapMarkOverlay;
		Button save = (Button) findViewById(R.id.Save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (fmapMarkOverlay.mapMark == null) {
					Toast.makeText(EditMapMark.this, R.string.place_marker, Toast.LENGTH_SHORT).show();
					return;
				}
				double dlat = fmapMarkOverlay.mapMark.getPoint().getLatitudeE6() / 1E6;
				double dlon = fmapMarkOverlay.mapMark.getPoint().getLongitudeE6() / 1E6;
				String titlestring = title.getText().toString();

				String sproxradius = proxradius.getText().toString();
				if (sproxradius.equals(""))
					sproxradius = "0";
				if (id == -1) {
					long newid = db.insertMapMark(titlestring, dlat, dlon, color, proxenter.isChecked(), proxexit.isChecked(), Integer.parseInt(sproxradius), DBAdapter.MAPMARK);
					if (enabled)
						AlertSetter.setAlert(EditMapMark.this, newid, true);
				} else {
					db.updateMapMark(id, titlestring, dlat, dlon, color, proxenter.isChecked(), proxexit.isChecked(), Integer.parseInt(sproxradius));
					if (enabled) {
						// radius could have been changed so remove the old alert and set the new
						AlertSetter.setAlert(EditMapMark.this, id, false);
						AlertSetter.setAlert(EditMapMark.this, id, true);
					}
				}

				Toast.makeText(EditMapMark.this, R.string.mapmark_saved, Toast.LENGTH_SHORT).show();
				finish();
			}
		});

		Button proxalert = (Button) findViewById(R.id.proxalerts);
		proxalert.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				proximityButtonClicked();
			}
		});

		final ColorPickerDialog fcpd = cpd;
		colorButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				fcpd.show();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("color", color);
		outState.putInt("latitude", mapMarkOverlay.mapMark.getPoint().getLatitudeE6());
		outState.putInt("longitude", mapMarkOverlay.mapMark.getPoint().getLongitudeE6());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROXIMITY:
			return new AlertDialog.Builder(this).setTitle(R.string.proxalert).setView(proximity).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(0);
				}
			}).create();
		case UPGRADE:
			return new AlertDialog.Builder(this).setMessage(R.string.upgradetext).setPositiveButton(R.string.upgradebutton, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:nl.arnorob.pointpro")));
				}
			}).create();
		default:
			throw new IllegalArgumentException("not supported:" + id);
		}
	}

	public void colorChanged(int color) {
		this.color = color;
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

	protected void proximityButtonClicked() {
		showDialog(PROXIMITY);
	}
}
