package nl.arnorob.point.proximity;

import nl.arnorob.point.Point;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class Starter extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Uri uri = getIntent().getData();
		long id = Integer.parseInt(uri.getQueryParameter("id"));
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel((int)id);
		startActivity(new Intent(getApplicationContext(),Point.class));

	}
}
