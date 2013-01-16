package nl.arnorob.point.test;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import nl.arnorob.point.model.Trackable;
import junit.framework.TestCase;

public class TestDirection extends TestCase {

  public void testDir1() {

	Location greenwich = new Location(LocationManager.NETWORK_PROVIDER);
    greenwich.setLatitude(0);
    greenwich.setLongitude(52);
    Trackable trackable = new Trackable("greenwich line, west", Color.CYAN, greenwich);

    Location home = new Location(LocationManager.NETWORK_PROVIDER);
    home.setLatitude(52);
    home.setLongitude(4);

    trackable.update(home);
    
    float dir = trackable.getBearing();
    double len = trackable.getDistance();
    assertEquals(270, dir, 0.1);
  }
}
