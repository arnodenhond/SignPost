package nl.arnorob.point.db;

import nl.arnorob.pointpro.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

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

	public void createDefaults() {
		
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
	
	public void dropAll() {
		db.delete(MAPMARKS_TABLE, null, null);
	}

	private static class DBOpenHelper extends SQLiteOpenHelper {
		Context context;
		
		public DBOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			this.context=context;
		}

		private static final String MAPMARKSTABLE_CREATE = "create table " + MAPMARKS_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " + KEY_NAME + " text not null, " + KEY_LATITUDE + " real, " + KEY_LONGITUDE + " real, " + KEY_COLOR + " integer, " + KEY_POINTERENABLED + " integer, " + KEY_PROXENTER + " integer, " + KEY_PROXEXIT + " integer, " + KEY_PROXRADIUS + " integer, " + KEY_TYPE + " integer " + ");";

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(MAPMARKSTABLE_CREATE);
			
			db.execSQL(makeSpot(context.getString(R.string.spot1),Color.RED));
			db.execSQL(makeSpot(context.getString(R.string.spot2),Color.GREEN));
			db.execSQL(makeSpot(context.getString(R.string.spot3),Color.BLUE));
			//db.execSQL(makeMapMark(context.getString(R.string.mapmarkGoogleplex),37.422d,-122.084d, Color.BLUE));
			db.execSQL(makeMapMark("Papeete",-17.539d,-149.558d, Color.BLUE));
			db.execSQL(makeMapMark("Nuku Hiva",-8.89d,-140.12d, Color.LTGRAY));
			//
			db.execSQL(makeMapMark(context.getString(R.string.mapmarkMtEverest),27.980d,86.926d, Color.GREEN));
			db.execSQL(makeMapMark(context.getString(R.string.mapmarkMtKilimanjaro),-3.065d,37.358d, Color.YELLOW));
			db.execSQL(makeMapMark(context.getString(R.string.mapmarkAyersRock),-25.345d,131.038d, Color.RED));
			db.execSQL(makeMapMark(context.getString(R.string.mapmarkMachuPicchu),-13.163d, -72.545d, Color.CYAN));
			db.execSQL(makeMapMark(context.getString(R.string.mapmarkEiffelTower),48.858d,2.294d, Color.MAGENTA));
		}

		private String makeMapMark(String name, double lat, double lon, int color) {
			return 	"insert into "+MAPMARKS_TABLE+" ("
			+KEY_NAME+","
			+KEY_LATITUDE+","
			+KEY_LONGITUDE+","
			+KEY_COLOR+","
			+KEY_POINTERENABLED+","
			+KEY_PROXENTER+","
			+KEY_PROXEXIT+","
			+KEY_PROXRADIUS+","
			+KEY_TYPE
		+" ) values ("
			+"'"+name+"',"
			+"'"+lat+"',"
			+"'"+lon+"',"
			+color+","
			+ON+","
			+OFF+","
			+OFF+","
			+"0,"
			+MAPMARK
		+")";
		}
		
		private String makeSpot(String name, int color) {
			return 	"insert into "+MAPMARKS_TABLE+" ("
					+KEY_NAME+","
					+KEY_LATITUDE+","
					+KEY_LONGITUDE+","
					+KEY_COLOR+","
					+KEY_POINTERENABLED+","
					+KEY_PROXENTER+","
					+KEY_PROXEXIT+","
					+KEY_PROXRADIUS+","
					+KEY_TYPE
				+" ) values ("
					+"'"+name+"',"
					+"'0',"
					+"'0',"
					+color+","
					+OFF+","
					+OFF+","
					+OFF+","
					+"0,"
					+SPOT
				+")";
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + MAPMARKS_TABLE);
			onCreate(db);
		}

	}

}
