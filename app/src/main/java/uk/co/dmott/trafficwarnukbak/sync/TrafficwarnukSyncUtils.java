package uk.co.dmott.trafficwarnukbak.sync;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import uk.co.dmott.trafficwarnukbak.R;
import uk.co.dmott.trafficwarnukbak.data.TrafficContract;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by david on 21/03/17.
 */

public class TrafficwarnukSyncUtils {

    private static final int SYNC_INTERVAL_SECONDS = 120;
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static boolean sInitialized;

    private static final String TAG = TrafficwarnukSyncUtils.class.getSimpleName();

    private static final String TRAFFIC_SYNC_TAG = "trafficwarnuk-sync";
    private static LocationManager locationManager;



    /**
     * Schedules a repeating sync of traffic data using FirebaseJobDispatcher.
     * @param context Context used to create the GooglePlayDriver that powers the
     *                FirebaseJobDispatcher
     */
    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Integer updateInterval = Integer.parseInt(preferences.getString(context.getResources().getString(R.string.pref_traffic_update_interval_key), "600"));

        Log.d(TAG, "newJobBuilder : Setting traffic update interval to " + updateInterval + "seconds");


        /* Create the Job to periodically sync Traffic*/
        Job syncTrafficJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync traffic warn uk data */
                .setService(uk.co.dmott.trafficwarnukbak.sync.TrafficwarnukFirebaseJobService.class)
                /* Set the UNIQUE tag used to identify this Job */
                .setTag(TRAFFIC_SYNC_TAG)
                /*
                 * Network constraints on which this Job should run. We choose to run on any
                 * network, but you can also choose to run only on un-metered networks or when the
                 * device is charging. It might be a good idea to include a preference for this,
                 * as some users may not want to download any data on their mobile plan. ($$$)
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want trafficwarnuk's  data to stay up to date, so we tell this Job to recur.
                 */
                .setRecurring(true)
                /*
                 * We want the trafic data to be synced time specified by the user. The first argument for
                 * Trigger's static executionWindow method is the start of the time frame when the
                 * sync should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */


                .setTrigger(Trigger.executionWindow(
                        updateInterval,
                        updateInterval + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag with provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

        /* Schedule the Job with the dispatcher */
        dispatcher.schedule(syncTrafficJob);
    }



/**
    public static Location getLastKnownLocation2(Context myContext) {
        locationManager = (LocationManager) myContext.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        if (ContextCompat.checkSelfPermission(myContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "getLastKnownLocation2 - Location Permission not granted");

        }
        try {

            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            return bestLocation;
        } catch (Exception ex) {
            Log.e(TAG, "getLastKnownLocation2 - getLastKnownLocation has failed!");


        }
        return bestLocation;
    }

*/
    public static Location[] getLastKnownLocation(Context myContext) {
        Location lastKnownGPSLocation;
        Location lastKnownNetworkLocation;
        Location [] locations = {null, null};
        String gpsLocationProvider = LocationManager.GPS_PROVIDER;
        String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

        if (ContextCompat.checkSelfPermission(myContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {

            Log.i(TAG,"Location Permission not granted");

        }


        try {
            locationManager = (LocationManager) myContext.getSystemService(LOCATION_SERVICE);

            lastKnownNetworkLocation = locationManager.getLastKnownLocation(networkLocationProvider);
            lastKnownGPSLocation = locationManager.getLastKnownLocation(gpsLocationProvider);

            if (lastKnownGPSLocation != null) {
                Log.i(TAG, "lastKnownGPSLocation is used.");
                //sCurrentLocation = lastKnownGPSLocation;
                locations[0] = lastKnownGPSLocation;
            } else if (lastKnownNetworkLocation != null) {
                Log.i(TAG, "lastKnownNetworkLocation is used.");
                //sCurrentLocation = lastKnownNetworkLocation;
                locations[1] = lastKnownGPSLocation;
            } else {
                Log.e(TAG, "lastLocation is not known.");
                return locations;
            }


        } catch (SecurityException sex) {
            Log.e(TAG, "Location permission is not granted!");
        }

        return locations;
    }


    public static Double calculateDistanceFromCurrentLocation(double lat1, double lon1, double lat2, double lon2, char measureType) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; // gets distance in miles


        if (measureType == 'K') {
            dist = dist * 1.609344;
        } else if (measureType ==  'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    public static Double convertMilesToKilometers(double valueinMiles)
    {
        return (valueinMiles * 1.609344);

    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::  This function converts decimal degrees to radians             :*/
/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::  This function converts radians to decimal degrees             :*/
/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    synchronized public static void initialize(@NonNull final Context context) {

        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
        if (sInitialized) {

            scheduleFirebaseJobDispatcherSync(context); // since we can change in options
            return;
        }

        sInitialized = true;
        //sContext = context;

        /*
         * This method call triggers Sunshine to create its task to synchronize weather data
         * periodically.
         */
        scheduleFirebaseJobDispatcherSync(context);

        /*
         * We need to check to see if our ContentProvider has data to display in our forecast
         * list. However, performing a query on the main thread is a bad idea as this may
         * cause our UI to lag. Therefore, we create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {

                /* URI for every row of weather data in our weather table*/
                Uri forecastQueryUri = TrafficContract.TrafficEntry.CONTENT_URI;

                /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what weather details need to be displayed.
                 */
                String[] projectionColumns = {TrafficContract.TrafficEntry._ID};
                //String selectionStatement = TrafficContract.TrafficEntry
                //        .getSqlSelectForTodayOnwards();

                /* Here, we perform the query to check to see if we have any traffic data */
                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        null,
                        null,
                        null);
                /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */

                // temporary while debugging startImmediateSync


//                if (null == cursor || cursor.getCount() == 0) {
                startImmediateSync(context);

//                    System.out.println("Need to insert data");



//                }
//                else
///                {
//                    System.out.println("Number of entries = " + cursor.getCount());
//                    System.out.println("Debug ");
//                }

                /* Make sure to close the Cursor to avoid memory leaks! */
                cursor.close();
            }
        });

        /* Finally, once the thread is prepared, fire it off to perform our checks. */
        checkForEmpty.start();
    }

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, TrafficSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }


}
