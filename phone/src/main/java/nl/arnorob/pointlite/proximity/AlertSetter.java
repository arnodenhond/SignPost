package nl.arnorob.pointlite.proximity;

import nl.arnorob.pointlite.db.DBAdapter;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;

public class AlertSetter extends BroadcastReceiver {

	public static void setAlert(Context context, long id, boolean enabled) {
		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor cursor = db.getMapMark(id);
		cursor.moveToFirst();
		double lat = cursor.getDouble(DBAdapter.LATITUDE_COLUMN);
		double lon = cursor.getDouble(DBAdapter.LONGITUDE_COLUMN);
		int radius = cursor.getInt(DBAdapter.PROXRADIUS_COLUMN);
		boolean enteron = cursor.getInt(DBAdapter.PROXENTER_COLUMN)==DBAdapter.ON;
		boolean exiton = cursor.getInt(DBAdapter.PROXEXIT_COLUMN)==DBAdapter.ON;
		cursor.close();
		db.close();

		Uri uri = Uri.parse("point://alert?id=" + id);
		Intent intent = new Intent("PointAlert", uri);
		PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, Intent.FLAG_GRANT_READ_URI_PERMISSION);

		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (enabled && (enteron || exiton))
			lm.addProximityAlert(lat, lon, radius, -1, broadcast);
		else
			lm.removeProximityAlert(broadcast);
	}

	@Override
	public void onReceive(Context context, Intent receivedIntent) {
		DBAdapter adapter = new DBAdapter(context);
		adapter.open();
		Cursor cursor = adapter.getEnabledMapMarksCursor();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(DBAdapter.ID_COLUMN);
			setAlert(context, id, true);
			cursor.moveToNext();
		}
		cursor.close();
		adapter.close();
	}

}
