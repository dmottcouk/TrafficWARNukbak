package uk.co.dmott.trafficwarnukbak;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import uk.co.dmott.trafficwarnukbak.sync.PositionSyncTask;

import static com.google.android.gms.location.LocationRequest.PRIORITY_NO_POWER;

/**
 * Created by david on 05/03/17.
 */

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationService";

    //private String defaultUploadWebsite;

    //private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        //       if (!currentlyProcessingLocation) {
        //           currentlyProcessingLocation = true;
        mContext = getApplicationContext();
        startTracking();
        //      }

        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d(TAG, "Location Service : startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            SharedPreferences.Editor ed = preferences.edit();
            Date currentDate=new Date();

            ed.putLong("longitude", Double.doubleToRawLongBits(location.getLongitude()));
            ed.putLong("latitude", Double.doubleToRawLongBits(location.getLatitude()));
            ed.putLong("positionobtaineddate", currentDate.getTime());

            ed.commit();
            Log.i(TAG, "Location committed to SharedPreferences in Locationservice ");

            // new addition - rewrite the distance values to the database

            // TODO: THIS NEEDS TESTING/ DEBUGGING .

            new PositionSyncTask(mContext).execute();

            stopLocationUpdates();


        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Log.i(TAG, "Disconnecting from googleApiClient in Locationservice ");
            googleApiClient.disconnect();
        }
        Log.i(TAG, "Calling stopself in Locationservice ");
        stopSelf();
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());
        Integer locationPriority = Integer.parseInt(preferences.getString(getApplicationContext().getResources().getString(R.string.pref_location_priority_type_key), "102"));

        Log.d(TAG, "newJobBuilder : Setting location update priority to " + locationPriority);

        switch(locationPriority){
            case 102:
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                Log.d(TAG, "newJobBuilder : Setting location update priority to PRIORITY_BALANCED_POWER_ACCURACY");
                break;
            case 100:
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                Log.d(TAG, "newJobBuilder : Setting location update priority to PRIORITY_HIGH_ACCURACY");
                break;
            case 104:
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                Log.d(TAG, "newJobBuilder : Setting location update priority to PRIORITY_LOW_POWER");
                break;
            case 105:
                locationRequest.setPriority(PRIORITY_NO_POWER);
                Log.d(TAG, "newJobBuilder : Setting location update priority to PRIORITY_NO_POWER");
                break;
            default:
                Log.d(TAG, "newJobBuilder : Setting location update priority to default!!!!!!!");
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }


        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        }catch (SecurityException sex){
            Log.e(TAG, "Security exception for TrafficWarnUK LocationService");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        //stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }
}
