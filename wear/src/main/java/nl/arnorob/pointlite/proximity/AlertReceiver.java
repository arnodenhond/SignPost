package nl.arnorob.pointlite.proximity;

import java.text.MessageFormat;

import nl.arnorob.pointlite.R;
import nl.arnorob.pointlite.db.DBAdapter;
import nl.arnorob.pointlite.view.FlatlandView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;

public class AlertReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Uri uri = intent.getData();
		long id = Integer.parseInt(uri.getQueryParameter("id"));

		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor cursor = db.getMapMark(id);
		cursor.moveToFirst();
		String title = cursor.getString(DBAdapter.NAME_COLUMN);
		int radius = cursor.getInt(DBAdapter.PROXRADIUS_COLUMN);
		boolean proxenter = cursor.getInt(DBAdapter.PROXENTER_COLUMN)==DBAdapter.ON;
		boolean proxexit = cursor.getInt(DBAdapter.PROXEXIT_COLUMN)==DBAdapter.ON;
		cursor.close();
		db.close();
		
		String message = "";
		boolean entering = intent.getExtras().getBoolean(LocationManager.KEY_PROXIMITY_ENTERING);
		if (entering) {
			if (!proxenter)
				return;
			message = MessageFormat.format(context.getString(R.string.proxenter), FlatlandView.getDistanceString(radius,FlatlandView.MODE_KM));
		} else {
			if (!proxexit)
				return;
			message = MessageFormat.format(context.getString(R.string.proxexit), FlatlandView.getDistanceString(radius,FlatlandView.MODE_KM));
		}
		
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(android.R.drawable.stat_notify_more, title, System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

//		PendingIntent pendingintent = PendingIntent.getActivity(context, 0, new Intent("Notified",uri), Intent.FLAG_ACTIVITY_NEW_TASK);
		//notification.setLatestEventInfo(context, title, message, pendingintent);

		//nm.notify((int) id, notification);
	}
}
