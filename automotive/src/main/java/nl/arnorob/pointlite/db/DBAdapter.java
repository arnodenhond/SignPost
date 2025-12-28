package nl.arnorob.pointlite.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBAdapter {

	public static final String KEY_ID = "_id";
	public static final int ID_COLUMN = 0;
	public static final String KEY_NAME = "name";
	public static final int NAME_COLUMN = 1;
	public static final String KEY_LATITUDE = "latitude";
	public static final int LATITUDE_COLUMN = 2;
	public static final String KEY_LONGITUDE = "longitude";
	public static final int LONGITUDE_COLUMN = 3;
	public static final String KEY_COLOR = "color";
	public static final int COLOR_COLUMN = 4;
	public static final String KEY_POINTERENABLED = "pointerenabled";
	public static final int POINTERENABLED_COLUMN = 5;
	public static final String KEY_PROXENTER = "proxenter";
	public static final int PROXENTER_COLUMN = 6;
	public static final String KEY_PROXEXIT = "proxexit";
	public static final int PROXEXIT_COLUMN = 7;
	public static final String KEY_PROXRADIUS = "proxradius";
	public static final int PROXRADIUS_COLUMN = 8;
	public static final String KEY_TYPE = "type";
	public static final int TYPE_COLUMN = 9;

	public static final int ON = 1;
	public static final int OFF = 0;

	public static final int MAPMARK = 0;
	public static final int SPOT = 1;
	public static final int BUDDY = 2;
	
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "point.db";
	private static final String MAPMARKS_TABLE = "mapmarks";

	private SQLiteDatabase db;
	private DBOpenHelper dbHelper;

	public DBAdapter(Context context) {
		dbHelper = new DBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void close() {
		db.close();
	}

	public void open() throws SQLiteException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}

	public Cursor getMapMarksCursor() {
		return db.query(MAPMARKS_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE, KEY_COLOR, KEY_POINTERENABLED }, null, null, null, null, null);
	}

	public Cursor getEnabledMapMarksCursor() {
		return db.query(MAPMARKS_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE, KEY_COLOR, KEY_POINTERENABLED, KEY_PROXENTER, KEY_PROXEXIT, KEY_PROXRADIUS }, KEY_POINTERENABLED + " = " + ON, null, null, null, null);
	}

	public Cursor getSpotMapMarksCursor() {
		return db.query(MAPMARKS_TABLE, new String[] { KEY_ID, KEY_NAME }, KEY_TYPE + " = " + SPOT, null, null, null, null);
	}

	public int setSpotMapMark(long id, double latitude, double longitude) {
		ContentValues bmValues = new ContentValues();
		bmValues.put(KEY_LATITUDE, latitude);
		bmValues.put(KEY_LONGITUDE, longitude);
		bmValues.put(KEY_POINTERENABLED, ON);
		return db.update(MAPMARKS_TABLE, bmValues, KEY_ID + " = " + id, null);
	}
	
	public Cursor getMapMark(long id) {
		return db.query(MAPMARKS_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE, KEY_COLOR, KEY_POINTERENABLED, KEY_PROXENTER, KEY_PROXEXIT, KEY_PROXRADIUS }, KEY_ID + " = " + id, null, null, null, null);
	}

	public long deleteMapMark(long id) {
		return db.delete(MAPMARKS_TABLE, KEY_ID + "=" + id, null);
	}

	public long setMapMarkEnabled(long id, boolean enabled) {
		ContentValues bmValues = new ContentValues();
		bmValues.put(KEY_POINTERENABLED, enabled ? ON : OFF);
		return db.update(MAPMARKS_TABLE, bmValues, KEY_ID + " = " + id, null);
	}

	public long updateMapMark(long id, String name, double latitude, double longitude, int color, boolean proxenter, boolean proxexit, int proxradius) {
		ContentValues bmValues = new ContentValues();
		bmValues.put(KEY_NAME, name);
		bmValues.put(KEY_LATITUDE, latitude);
		bmValues.put(KEY_LONGITUDE, longitude);
		bmValues.put(KEY_COLOR, color);
		bmValues.put(KEY_PROXENTER, proxenter ? ON : OFF);
		bmValues.put(KEY_PROXEXIT, proxexit ? ON : OFF);
		bmValues.put(KEY_PROXRADIUS, proxradius);
		return db.update(MAPMARKS_TABLE, bmValues, KEY_ID + " = " + id, null);
	}

	public long insertMapMark(String name, double latitude, double longitude, int color, boolean proxenter, boolean proxexit, int proxradius, int type) {
		ContentValues bmValues = new ContentValues();
		bmValues.put(KEY_NAME, name);
		bmValues.put(KEY_LATITUDE, latitude);
		bmValues.put(KEY_LONGITUDE, longitude);
		bmValues.put(KEY_COLOR, color);
		bmValues.put(KEY_POINTERENABLED, ON);
		bmValues.put(KEY_PROXENTER, proxenter ? ON : OFF);
		bmValues.put(KEY_PROXEXIT, proxexit ? ON : OFF);
		bmValues.put(KEY_PROXRADIUS, proxradius);
		bmValues.put(KEY_TYPE, type);
		return db.insert(MAPMARKS_TABLE, null, bmValues);
	}

	private static class DBOpenHelper extends SQLiteOpenHelper {
		
		public DBOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		private static final String MAPMARKSTABLE_CREATE = "create table " + MAPMARKS_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " + KEY_NAME + " text not null, " + KEY_LATITUDE + " real, " + KEY_LONGITUDE + " real, " + KEY_COLOR + " integer, " + KEY_POINTERENABLED + " integer, " + KEY_PROXENTER + " integer, " + KEY_PROXEXIT + " integer, " + KEY_PROXRADIUS + " integer, " + KEY_TYPE + " integer " + ");";

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(MAPMARKSTABLE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + MAPMARKS_TABLE);
			onCreate(db);
		}

	}

}
