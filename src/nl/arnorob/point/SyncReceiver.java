package nl.arnorob.point;

import nl.arnorob.point.db.DBAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

public class SyncReceiver extends BroadcastReceiver {

	DBAdapter db;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		db = new DBAdapter(context);
		db.open();
		db.dropAll();
		try {
			JSONTokener tokener = new JSONTokener(intent.getStringExtra("it.imwatch.JSON_DATA"));
			JSONObject object = (JSONObject) tokener.nextValue();
			object = object.getJSONObject("configurationValues");
			String ownlocation = object.getString("KEY_OWNLOCATION");
			
			String[] latlon = ownlocation.split(",");
			double lat = Double.parseDouble(latlon[0]);
			double lon = Double.parseDouble(latlon[1]);
			
			SharedPreferences.Editor edit = context.getSharedPreferences("MYLOCATION", Context.MODE_PRIVATE).edit();
			edit.putFloat("LAT", (float) lat);
			edit.putFloat("LON", (float) lon);
			edit.commit();

			for (int i = 1; i < 10; i++) {
				doPointer(i, object);
			}
		} catch (JSONException jse) {
			jse.toString();
		}
		db.close();
	}

	private void doPointer(int i, JSONObject object) throws JSONException {
		String name = object.getString("KEY_POINTER"+i+"NAME");
		String[] latlon = object.getString("KEY_POINTER"+i+"LOCATION").split(",");
		double lat = Double.parseDouble(latlon[0]);
		double lon = Double.parseDouble(latlon[1]);
		String color = object.getString("KEY_POINTER"+i+"COLOR");
		db.insertMapMark(name, lat, lon, color(color), false, false, 0, DBAdapter.MAPMARK);
	}
	
	private int color(String scolor) {
		switch (Integer.parseInt(scolor)) {
		case 0:return Color.RED;
		case 1:return Color.GREEN;
		case 2:return Color.BLUE;
		case 3:return Color.CYAN;
		case 4:return Color.MAGENTA;
		case 5:return Color.YELLOW;
		}
		return Color.WHITE;
	}

}
