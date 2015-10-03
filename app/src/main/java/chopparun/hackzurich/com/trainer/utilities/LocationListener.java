package chopparun.hackzurich.com.trainer.utilities;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

/**
 * Created by Evelyn on 03/10/15.
 */
public class LocationListener implements android.location.LocationListener,GpsStatus.Listener {

    protected long mLastLocationMillis;
    protected Location mLastLocation;
    public float speed;
    protected LocationManager locationManager;

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) return;
        mLastLocationMillis = SystemClock.elapsedRealtime();

        /* Do something. */

        mLastLocation = location;
        speed = location.getSpeed();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}
