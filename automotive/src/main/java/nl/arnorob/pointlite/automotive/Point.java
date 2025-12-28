package nl.arnorob.pointlite.automotive;

import nl.arnorob.pointlite.automotive.mapmark.AutoMapMarks;
import nl.arnorob.pointlite.automotive.mapmark.EditMapMark;
import nl.arnorob.pointlite.automotive.mapmark.MapMarkList;
import nl.arnorob.pointlite.automotive.view.FlatlandView;
import nl.arnorob.pointlite.db.DBAdapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class Point extends Activity {

	private static final int MARK_SPOT = 1;
	private static final int ABOUT = 2;
	private static final int WELCOME = 3;
	private static final int BUDDIES = 4;
	private static final int INSTRUCTIONS = 5;
	

	protected static final int UPGRADE = 6;

	public static final String EDITMAPMARK = "EditMapMark";
	
	private TrackManager trackManager;
	protected FlatlandView flatlandView;
	private CompassManager compassManager;
	ListView spotlist;
	Toast accuracyToast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Implement proper permission handling for Android Auto
			return;
		}

		try {

			trackManager = new TrackManager(this);
			compassManager = new CompassManagerNew(this);
			flatlandView = new FlatlandView(this, trackManager, compassManager);

			doLayout();
			setActivities();

			spotlist = new ListView(this);
			TextView addTV = new TextView(this);
			addTV.setText("New Spot");
			addTV.setPadding(5, 5, 5, 5);
			addTV.setTextSize(20);
			addTV.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					try {
						dismissDialog(MARK_SPOT);
						DBAdapter db = new DBAdapter(Point.this);
						db.open();
						Location location = trackManager.getLocation();
						double latitude = location.getLatitude();
						double longitude = location.getLongitude();
						long rowid = db.insertMapMark("New Spot", latitude, longitude, Color.RED, false, false, 0, DBAdapter.SPOT);
						db.close();
						SharedPreferences prefs = getSharedPreferences("mapmark", MODE_PRIVATE);
						SharedPreferences.Editor edit = prefs.edit();
						edit.putLong("mapmark", rowid);
						edit.commit();
						startActivity(new Intent(getApplicationContext(),getClassForPreference(Point.this, EDITMAPMARK)));
					} catch (Throwable t) {
						Log.e(getClass().getSimpleName(), "new spot onClick", t);
					}

				}
			});
			spotlist.addHeaderView(addTV);

		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onCreate", t);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			trackManager.setTrackables(MapMarkList.getTrackables(Point.this));
			trackManager.resume();
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onResume", t);
		}
		try {
			compassManager.resume();
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onResume", t);
		}

	}

	@Override
	protected void onPause() {
		try {
			compassManager.pause();
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onpause", t);
		}
		try {
			trackManager.pause();
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onpause", t);
		}
		super.onPause();
	}

	protected void doLayout() {
		setContentView(flatlandView);
		flatlandView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Point.this, AutoMapMarks.class);
				Location loc = trackManager.getLocation();
				intent.putExtra("latitude", (float) loc.getLatitude());
				intent.putExtra("longitude",(float) loc.getLongitude());
				startActivity(intent);
			}
		});
	}
	
	protected void setActivities(){
		SharedPreferences prefs = getSharedPreferences("point", MODE_PRIVATE);
		SharedPreferences.Editor edit=prefs.edit();
		edit.putString(EDITMAPMARK, EditMapMark.class.getName());
		edit.commit();
	}
	
	public static Class getClassForPreference(Context c,String name) throws ClassNotFoundException{
		SharedPreferences prefs = c.getSharedPreferences("point", MODE_PRIVATE);
		String className=prefs.getString(name, "null");
		Class mapclazz = Class.forName(className);
		return mapclazz;
	}
	
	protected void markSpotSelected() {
			DBAdapter db = new DBAdapter(this);
			db.open();
			Cursor cursor = db.getSpotMapMarksCursor();
			SimpleCursorAdapter sca = new SimpleCursorAdapter(Point.this, android.R.layout.simple_list_item_1, cursor, new String[] { DBAdapter.KEY_NAME }, new int[] { android.R.id.text1 });
			spotlist.setAdapter(sca);
			db.close();
			accuracyToast = Toast.makeText(Point.this, "Accuracy: "+FlatlandView.getDistanceString((int)trackManager.getLocation().getAccuracy(),FlatlandView.MODE_KM), Toast.LENGTH_SHORT);
			showDialog(MARK_SPOT);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		try {
			Resources res = getResources();
			switch (id) {
			case WELCOME:
				return new AlertDialog.Builder(this).setTitle("Welcome").setMessage("Short Intro").setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(WELCOME);
					}
				}).create();

			case INSTRUCTIONS:
				ScrollView sv = new ScrollView(this);
				TextView welcometv = new TextView(this);
				welcometv.setText("Intro");
				sv.addView(welcometv);
				return new AlertDialog.Builder(this).setTitle("Instructions").setView(sv).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(INSTRUCTIONS);
					}
				}).create();
			case UPGRADE:
				return new AlertDialog.Builder(this).setMessage("Upgrade Text").setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:nl.arnorob.pointlite")));
					}
				}).create();
			case ABOUT:
				TextView abouttv = new TextView(this);
				abouttv.setText("About Text");
				abouttv.setGravity(Gravity.CENTER_HORIZONTAL);
				Linkify.addLinks(abouttv, Linkify.EMAIL_ADDRESSES);
				String title = "Point" + " v" + "1.0";
				return new AlertDialog.Builder(this).setTitle(title).setView(abouttv).setPositiveButton("More Apps", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(ABOUT);
						Intent email = new Intent(Intent.ACTION_SEND);
						email.putExtra(Intent.EXTRA_EMAIL, new String[] { "pointandroid@gmail.com" });
						email.setType("message/rfc822");
						startActivity(email);
					}
				}).create();
			case MARK_SPOT:
				spotlist.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
						Location location = trackManager.getLocation();
						double latitude = location.getLatitude();
						double longitude = location.getLongitude();
						DBAdapter db = new DBAdapter(Point.this);
						db.open();
						db.setSpotMapMark(id, latitude, longitude);
						db.close();
						onResume();
						dismissDialog(MARK_SPOT);
						accuracyToast.show();
					}
				});
				return new AlertDialog.Builder(this).setView(spotlist).create();
			}
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onCreateDialog", t);
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			recreate();
		}  else {
			// Explain to the user that the feature is unavailable because
			// the feature requires a permission that the user has denied.
			// At the same time, respect the user's decision. Don't link to
			// system settings in an effort to convince the user to change
			// their decision.
		}
	}


}