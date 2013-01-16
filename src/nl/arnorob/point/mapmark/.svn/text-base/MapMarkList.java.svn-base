package nl.arnorob.point.mapmark;

import nl.arnorob.point.Point;
import nl.arnorob.point.db.DBAdapter;
import nl.arnorob.point.model.Trackable;
import nl.arnorob.point.proximity.AlertSetter;
import nl.arnorob.pointpro.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class MapMarkList extends ListActivity {

	private static int DELETE = Menu.FIRST;
	private static int EDIT = Menu.FIRST + 1;

	private long mmid;
	private DBAdapter db;
	private Cursor cursor;

	public static Trackable[] getTrackables(Context context) {
		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor mapmarks = db.getEnabledMapMarksCursor();
		Trackable[] tr = new Trackable[mapmarks.getCount()];
		mapmarks.moveToFirst();
		for (int i = 0; i < tr.length; i++) {
			int color = mapmarks.getInt(DBAdapter.COLOR_COLUMN);
			String title = mapmarks.getString(DBAdapter.NAME_COLUMN);
			double lat = mapmarks.getDouble(DBAdapter.LATITUDE_COLUMN);
			double lon = mapmarks.getDouble(DBAdapter.LONGITUDE_COLUMN);
			long id = mapmarks.getLong(DBAdapter.ID_COLUMN);
			Location location = new Location("point");
			location.setLatitude(lat);
			location.setLongitude(lon);
			Trackable t = new Trackable(title, color, location,id );
			tr[i] = t;
			mapmarks.moveToNext();
		}
		mapmarks.close();
		db.close();
		return tr;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DBAdapter(this);
		db.open();
		cursor = db.getMapMarksCursor();
		startManagingCursor(cursor);
		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, cursor, new String[] { DBAdapter.KEY_NAME }, new int[] { android.R.id.text1 });
		setListAdapter(sca);
		setContentView(getListView());

		TextView emptyTV = new TextView(this);
		emptyTV.setId(android.R.id.empty);
		emptyTV.setText(R.string.nomapmarks);
		emptyTV.setTextSize(25);
		emptyTV.setGravity(Gravity.CENTER);
		addContentView(emptyTV, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		getListView().setLongClickable(true);
		registerForContextMenu(getListView());
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
				mmid = id;
				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Cursor cc = db.getMapMarksCursor();
		cc.moveToFirst();
		ListView lv = getListView();
		for (int i = 0; i < cc.getCount(); i++) {
			lv.setItemChecked(i, cc.getInt(DBAdapter.POINTERENABLED_COLUMN) == DBAdapter.ON);
			cc.moveToNext();
		}
		cc.close();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(MapMarkList.this).setTitle(R.string.delete).setMessage(R.string.confirm_del).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				AlertSetter.setAlert(MapMarkList.this, mmid, false);
				db.deleteMapMark(mmid);
				cursor.requery();
				Toast.makeText(MapMarkList.this, R.string.mapmark_deleted, Toast.LENGTH_SHORT).show();
			}
		}).setNegativeButton(R.string.cancel, null).create();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, EDIT, 0, R.string.edit);
		menu.add(0, DELETE, 0, R.string.delete);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		try {
			if (item.getItemId() == DELETE) {
				showDialog(0);
			}
			if (item.getItemId() == EDIT) {
				SharedPreferences prefs = getSharedPreferences("mapmark", MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putLong("mapmark", mmid);
				edit.commit();
				// Class mapclazz = Class.forName("nl.arnorob.point.mapmark.EditMapMark");
				// Log.i("loaded class",""+mapclazz);
				startActivity(new Intent(getApplicationContext(), Point.getClassForPreference(this, Point.EDITMAPMARK)));// EditMapMark.class));
				// startActivity(new Intent("EditMapMark"));
			}
			return true;
		} catch (Throwable t) {
			Log.e("" + getClass().getSimpleName(), "", t);
			return false;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		boolean enabled = getListView().isItemChecked(position);
		db.setMapMarkEnabled(id, enabled);
		AlertSetter.setAlert(this, id, enabled);
	}

	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
	}

}
