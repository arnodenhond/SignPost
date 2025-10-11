package nl.arnorob.pointlite;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.View;

public class ViewUpdater {
	private final Activity a;

	public ViewUpdater(Activity a) {
		this.a = a;
	}

	private final List<View> views = new ArrayList<View>();

	/**
	 * Called by a view if it wants to be invalidated by TrackManager when the location has changed.
	 * 
	 * @param view
	 *            the view to be invalidated.
	 */
	public void requestInvalidates(View view) {
		if (!views.contains(view)) {
			views.add(view);
		}
	}

	protected void invalidateViews() {
		a.runOnUiThread(new Runnable() {
			public void run() {
				try {
					for (View view : views) {
						view.invalidate();
					}
				} catch (Throwable t) {
					Log.e("ViewUpdater", "" + getClass().getSimpleName(), t);
				}
			}
		});
	}
}