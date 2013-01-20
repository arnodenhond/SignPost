package nl.arnorob.point;

import nl.arnorob.point.db.DBAdapter;
//import nl.arnorob.point.mapmark.EditMapMark;
import nl.arnorob.point.mapmark.MapMarkList;
import nl.arnorob.point.view.FlatlandView;
import nl.arnorob.pointpro.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
		try {
			super.onCreate(savedInstanceState);

			trackManager = new TrackManager(this);
			compassManager = new CompassManagerNew(this);
			flatlandView = new FlatlandView(this, trackManager, compassManager);

			doLayout();
			setActivities();

			spotlist = new ListView(this);
			TextView addTV = new TextView(this);
			addTV.setText(getString(R.string.newspot));
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
						long rowid = db.insertMapMark(getString(R.string.newspot), latitude, longitude, Color.RED, false, false, 0, DBAdapter.SPOT);
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

			SharedPreferences prefs = getSharedPreferences("firststart", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			if (prefs.getBoolean("firststart", true)) {
				editor.putBoolean("firststart", false);
				editor.commit();
				showDialog(WELCOME);
			}

			// compassManager.simulate();
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onCreate", t);
		}
	}

	@Override
	protected void onResume() {
		try {
			super.onResume();
			trackManager.setTrackables(MapMarkList.getTrackables(Point.this));
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
		
		super.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		try {
		menu.add(R.string.addmapmark).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(R.string.markspot).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(R.string.pointers).setIcon(android.R.drawable.ic_menu_compass);
		//menu.add(R.string.addbuddy).setIcon(android.R.drawable.ic_menu_myplaces);
		// menu.add("Switch View").setIcon(android.R.drawable.ic_menu_always_landscape_portrait);
		//menu.add("Settings").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(R.string.help).setIcon(android.R.drawable.ic_menu_help);
		menu.add(R.string.about).setIcon(android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onCreateOptionsMenu", t);
			return super.onCreateOptionsMenu(menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
		if (item.getTitle().equals(getString(R.string.pointers))) {
			startActivity(new Intent(getApplicationContext(),MapMarkList.class));
//			startActivity(new Intent("MapMarkList"));
		} else if (item.getTitle().equals(getString(R.string.addmapmark))) {
			SharedPreferences prefs = getSharedPreferences("mapmark", MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putLong("mapmark", -1);
			edit.commit();
			startActivity(new Intent(getApplicationContext(),getClassForPreference(this, EDITMAPMARK)));
			//startActivity(new Intent("EditMapMark"));
		} else if (item.getTitle().equals(getString(R.string.markspot))) {
				markSpotSelected();
		} else if (item.getTitle().equals(getString(R.string.about))) {
			showDialog(ABOUT);
		} else if (item.getTitle().equals(getString(R.string.help))) {
			showDialog(INSTRUCTIONS);
		} else if (item.getTitle().equals(getString(R.string.addbuddy))) {
			showDialog(BUDDIES);
		} else {
			//trackManager.setTrackables(TestTrackable.getTrackables());
		}
		
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "onOptionsItemSelected", t);
			return true;
		}
		return true;
	}		

	protected void doLayout() {
		setContentView(flatlandView);
	}
	
	protected void setActivities(){
		SharedPreferences prefs = getSharedPreferences("point", MODE_PRIVATE);
		SharedPreferences.Editor edit=prefs.edit();
		//edit.putString(EDITMAPMARK, EditMapMark.class.getName());
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
			startManagingCursor(cursor);
			SimpleCursorAdapter sca = new SimpleCursorAdapter(Point.this, android.R.layout.simple_list_item_1, cursor, new String[] { DBAdapter.KEY_NAME }, new int[] { android.R.id.text1 });
			spotlist.setAdapter(sca);
			db.close();
			if (!trackManager.isUsingGPS()) {
				Toast.makeText(this, getString(R.string.enablegps), Toast.LENGTH_SHORT).show();
			}
			accuracyToast = Toast.makeText(Point.this, getString(R.string.accuracy)+" "+FlatlandView.getDistanceString((int)trackManager.getLocation().getAccuracy(),FlatlandView.MODE_KM), Toast.LENGTH_SHORT);
			showDialog(MARK_SPOT);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		try {
			Resources res = getResources();
			switch (id) {
			case WELCOME:
				return new AlertDialog.Builder(this).setTitle(R.string.welcome).setMessage(R.string.shortintro).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(WELCOME);
					}
				}).create();

			case INSTRUCTIONS:
				ScrollView sv = new ScrollView(this);
				TextView welcometv = new TextView(this);
				welcometv.setText(R.string.intro);
				sv.addView(welcometv);
				return new AlertDialog.Builder(this).setTitle(R.string.instructions).setView(sv).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(INSTRUCTIONS);
					}
				}).create();
			case UPGRADE:
				return new AlertDialog.Builder(this).setMessage(R.string.upgradetext).setPositiveButton(R.string.upgradebutton, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:nl.arnorob.pointpro")));
					}
				}).create();
			case ABOUT:
				TextView abouttv = new TextView(this);
				abouttv.setText(R.string.abouttext);
				abouttv.setGravity(Gravity.CENTER_HORIZONTAL);
				Linkify.addLinks(abouttv, Linkify.EMAIL_ADDRESSES);
				String title = res.getString(R.string.app_name) + " v" + res.getText(R.string.point_version);
				return new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(title).setView(abouttv).setPositiveButton(R.string.moreapps, new DialogInterface.OnClickListener() {
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

}