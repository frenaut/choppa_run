package chopparun.hackzurich.com.trainer.utilities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import chopparun.hackzurich.com.trainer.RunTrackerService;

/**
 * Created by Evelyn on 03/10/15.
 */
public class _LocationListener implements LocationListener {
    private final String TAG = "_LocationListener";

    private RunTrackerService service_;
    protected long mLastLocationMillis;
    protected Location mLastLocation;

    public _LocationListener Create(RunTrackerService service) {
        return new _LocationListener(service_);
    }

    private GoogleApiClient client_;
    public _LocationListener(RunTrackerService service) {
        service_ = service;
        final _LocationListener this_ = this;

        // Connect to Google Play API
        client_ = new GoogleApiClient.Builder(service_)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        // Construct request
                        LocationRequest request = LocationRequest.create();
                        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        request.setInterval(1000);

                        // Listen to location changes
                        LocationServices.FusedLocationApi.requestLocationUpdates(client_, request, this_);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        client_.disconnect();
                        client_.connect();
                    }
                })
                .build();
        client_.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;
        //Log.d(TAG, "Location: " + location.toString());
        mLastLocationMillis = SystemClock.elapsedRealtime();

        if (mLastLocation != null) {
            float dist = mLastLocation.distanceTo(location);

            /* Do something. */
            service_.updateDistance(dist);
        }

        mLastLocation = location;
    }

    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client_, this);
    }
}
