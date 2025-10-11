package nl.arnorob.pointlite;

import android.view.View;

public interface CompassManager {
	public void resume() ;
	public void pause();
	public float getHeading();
	public void requestInvalidates(View view);
}
