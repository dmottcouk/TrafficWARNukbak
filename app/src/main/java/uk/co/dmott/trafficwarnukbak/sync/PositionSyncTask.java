package uk.co.dmott.trafficwarnukbak.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.co.dmott.trafficwarnukbak.R;
import uk.co.dmott.trafficwarnukbak.TrafficItemListActivity;
import uk.co.dmott.trafficwarnukbak.data.TrafficContract;
import uk.co.dmott.trafficwarnukbak.data.TrafficPojoEntry;
import uk.co.dmott.trafficwarnukbak.utilities.NotificationUtils;

import static android.R.attr.defaultValue;

/**
 * Created by david on 02/04/17.
 */
// Invoked on a background thread

public class PositionSyncTask extends AsyncTask<Void, Void, Cursor> {

    private static final String TAG = PositionSyncTask.class.getSimpleName();


    // variable to hold context
    private Context context;
    private static List<TrafficPojoEntry> mtrafficitems;
    private Double mCurrentLongitude;
    private Double mCurrentLatitude;

    private static Date mposObtainedDate = null;
    int contentValuesIndexCounter = 0;

    private Cursor myTrafficcursor;

    public PositionSyncTask(Context context) {
        this.context = context;
    }


    /**
     * while (cursor.h)
     * <p>
     * <p>
     * <p>
     * <p>
     * / Delete old traffic data because we don't need to keep multiple days' data /
     * trafficContentResolver.delete(
     * TrafficContract.TrafficEntry.CONTENT_URI,
     * null,
     * null);
     * <p>
     * / Insert our new traffic data  ContentProvider /
     * trafficContentResolver.bulkInsert(
     * TrafficContract.TrafficEntry.CONTENT_URI,
     * trafficContentValues);
     **/


    @Override
    protected Cursor doInBackground(Void... params) {
        ContentResolver trafficContentResolver = context.getContentResolver();

        Cursor positionUpdateCursor = trafficContentResolver.query(TrafficContract.TrafficEntry.CONTENT_URI, TrafficItemListActivity.MAIN_TRAFFIC_PROJECTION, null, null, null);

        Log.d(TAG, "Obtained the cursor for updating the database with updated position information information");

        int itemCount = positionUpdateCursor.getCount();

        Log.d(TAG, "Number of items retrieved in PositionSyncTask = " + itemCount);


        return positionUpdateCursor;
    }

    @Override
    protected void onPostExecute(Cursor cursor)
    {
        super.onPostExecute(cursor);
        List<TrafficPojoEntry> trafficList = new ArrayList<TrafficPojoEntry>();
        int notificationRadius = 0;
        int notificationInterval = 0;
        boolean preventNotification = false;
        boolean generateNotification = false; // dont generate notification by default
        boolean notificationDistanceInMiles = true;
        Double distanceNearestEventFromCurrentLocation = -1.0; // this is always in miles
        String notificationString = "";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(!preferences.contains("longitude")){
            Log.d(TAG, "There is no defaultpreference value for longitude in onPostExecute of PositionSyncTask");
            mCurrentLongitude = -999.0;
        }
        else
        {
            mCurrentLongitude = Double.longBitsToDouble(preferences.getLong("longitude", Double.doubleToLongBits(defaultValue)));
            String formattedCurrentLongitude = String.format("%.2f", mCurrentLongitude);
            Log.d(TAG, "current longitude in onPostExecute of PositionSyncTask is" + formattedCurrentLongitude);


        }

        if(!preferences.contains("latitude")){
            Log.d(TAG, "There is no defaultpreference value for latitude in onPostExecute of PositionSyncTask");
            mCurrentLatitude = -999.0;
        }
        else
        {
            mCurrentLatitude = Double.longBitsToDouble(preferences.getLong("latitude", Double.doubleToLongBits(defaultValue)));
            String formattedCurrentLatitude = String.format("%.2f", mCurrentLatitude);
            Log.d(TAG, "current latitude in onPostExecute of PositionSyncTask is" + formattedCurrentLatitude);


        }
        if ((mCurrentLongitude < -180.0) || (mCurrentLatitude < -180.0))
            return;


        if(!preferences.contains("positionobtaineddate")){
            Log.d(TAG, "There is no defaultpreference value for obtained date in onPostExecute of PositionSyncTask");

        }
        else
        {
            long millis = preferences.getLong("positionobtaineddate", 0L);
            mposObtainedDate = new Date(millis);

            Log.d(TAG, "position obtained date in onPostExecute of PositionSyncTask is" + mposObtainedDate.toString());

        }

        if(!preferences.contains("show_miles")){
            Log.d(TAG, "There is no defaultpreference value for preferred distance units in onPostExecute of PositionSyncTask");
        }
        else
        {
            notificationDistanceInMiles = preferences.getBoolean("show_miles", true);


            Log.d(TAG, "preferred distance units obtained in onPostExecute of PositionSyncTask");

        }
        notificationInterval  =   Integer.parseInt(preferences.getString(context.getResources().getString(R.string.pref_notification_interval_key), "600"))/ 60;
        Log.d(TAG, "Notification interval preference in mins in onPostExecute of PositionSyncTask =  " + notificationInterval);


        if(!preferences.contains("lastnotificationdate")){
            Log.d(TAG, "There is no defaultpreference value for lastnotificationdate in onPostExecute of PositionSyncTask");
        }
        else
        {
            long lastNotifDate = preferences.getLong("lastnotificationdate", 0L);

            Date dateNow = new Date();

            long dateDiff = dateNow.getTime() - lastNotifDate;
     /* FRIG */

            //              if( (dateDiff / (60 * 1000)) < 2) {
            if( (dateDiff / (60 * 1000)) < notificationInterval) {


                //
                preventNotification = true; // dont generate more than one every notificationInterval
                Log.d(TAG, "in onPostExecute of PositionSyncTask set prevent notification as not passed interval = " + notificationInterval + "minutes");
            }
            else {
                preventNotification = false;
                Log.d(TAG, "in onPostExecute of PositionSyncTask clear prevent notification as passed interval = " + notificationInterval + "minutes");
            }

        }



        if(!preferences.contains(context.getResources().getString(R.string.pref_notification_radius_key))){
            Log.d(TAG, "There is no defaultpreference value for pref_notification_radius in onPostExecute of PositionSyncTask");
        }
        else
        {
            String prefRadius= preferences.getString(context.getResources().getString(R.string.pref_notification_radius_key), "10");
        }

        notificationRadius  =   Integer.parseInt(preferences.getString(context.getResources().getString(R.string.pref_notification_radius_key), "10"));
        Log.d(TAG, "Notification radius preference in onPostExecute of PositionSyncTask=  " + notificationRadius);


        if ((cursor != null) && (cursor.getCount() > 0))
        {

            while(cursor.moveToNext())
            {
                
                String cat1 = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_CATEGORY1);
                String cat2 = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_CATEGORY2);
                String description = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_DESCRIPTION);
                String title = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_TITLE);
                String road = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_ROAD);
                String region = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_REGION);
                String county = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_COUNTY);

                Double lat = cursor.getDouble(TrafficItemListActivity.INDEX_TRAFFIC_LATITUDE);
                Double longit = cursor.getDouble(TrafficItemListActivity.INDEX_TRAFFIC_LONGITUDE);

                String overallstart = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_OVERALLSTART);
                String overallend = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_OVERALLEND);
                String eventstart = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_EVENTSTART);
                String eventend = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_EVENTEND);

                String link = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_LINK);
                Long pubdatel = cursor.getLong(TrafficItemListActivity.INDEX_TRAFFIC_PUBDATE);
                
                Date pubdate = new Date(pubdatel); // convert the integer to a date for the Pojo;
                
                String reference = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_REFERENCE);
                String guid = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_GUID);
                String author = cursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_AUTHOR);

                Double distance = cursor.getDouble(TrafficItemListActivity.INDEX_TRAFFIC_DISTANCE);
                // recalculate the distance based on the latitude and longitude from SharedPreferences

                Log.d(TAG, "distance being replaced in onPostExecute of PositionSyncTask is" + distance);
                

                distance = TrafficwarnukSyncUtils.calculateDistanceFromCurrentLocation(mCurrentLatitude, mCurrentLongitude,lat,longit,'M');

                Log.d(TAG, "new distance in onPostExecute of PositionSyncTask is" + distance);


                TrafficPojoEntry myTrEntry = new TrafficPojoEntry(cat1, cat2, description, title, road, region, county, lat, longit, overallstart, overallend,eventstart,eventend,link,pubdate,reference,guid,author,distance);

                trafficList.add(myTrEntry);
                
            }

            ContentValues[] trafficContentValues = new ContentValues[trafficList.size()];
            Double distanceFromCurrentLocation = -1.0; // this is always in miles

            for(TrafficPojoEntry ent: trafficList) {

                Calendar now = Calendar.getInstance();
                Date dateNow = now.getTime();
                ContentValues trafficValues = new ContentValues();


                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_CATEGORY1, ent.getCategory1());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_CATEGORY2, ent.getCategory2());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_DESCRIPTION, ent.getDescription());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_TITLE, ent.getTitle());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_ROAD, ent.getRoad());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_REGION, ent.getRegion());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_COUNTY, ent.getCounty());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_LATITUDE, ent.getLatitude());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_LONGITUDE, ent.getLongitude());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_OVERALLSTART, ent.getOverallstart());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_OVERALLEND, ent.getOverallend());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_EVENTSTART, ent.getEventstart());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_EVENTEND, ent.getEventend());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_LINK, ent.getLink());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_PUBDATE, ent.getPubdate().getTime());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_STOREDATE, dateNow.getTime());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_REFERENCE, ent.getReference());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_GUID, ent.getGuid());
                trafficValues.put(TrafficContract.TrafficEntry.COLUMN_AUTHOR, ent.getAuthor());
                distanceFromCurrentLocation = ent.getDistance();
                Log.d(TAG, "new distance in onPostExecute of PositionSyncTask is" + distanceFromCurrentLocation);

                if (distanceFromCurrentLocation >= 0)
                {
                    trafficValues.put(TrafficContract.TrafficEntry.COLUMN_DISTANCE, distanceFromCurrentLocation);
                    Log.d(TAG, "putting the distance into ContentValue = " + distanceFromCurrentLocation);
                    if ((distanceFromCurrentLocation < notificationRadius) &&  (preventNotification == false)) // if less than notificationRadius miles away send notification

     /* FRIG */

            //                          if ((distanceFromCurrentLocation < 90.0) &&  (preventNotification == false)) // if less than notificationRadius miles away send notification
                    {
                        //check if there is any evemt within notificationRadius miles of current location
                        //if there is generate a notification to the watch

                        String catg = ent.getCategory1();

                        Log.d(TAG, "in onPostExecute of PositionSyncTask Event is closer than the notificationRadius which is " + notificationRadius);


                        if (true)  // we are currently alerting on any event within specifidd radius
                        {

                            if ((distanceNearestEventFromCurrentLocation < 0) || (distanceFromCurrentLocation < distanceNearestEventFromCurrentLocation)) {


                                generateNotification = true;
                                if (notificationDistanceInMiles) {
                                    notificationString = catg + ": Distance away " + String.format("%.2f", distanceFromCurrentLocation) + " Miles" + " : Road - " + ent.getRoad();

                                } else {
                                    notificationString = catg + ": Distance away " + String.format("%.2f", TrafficwarnukSyncUtils.convertMilesToKilometers(distanceFromCurrentLocation)) + " Kilometers" + " : Road - " + ent.getRoad();

                                }
                                distanceNearestEventFromCurrentLocation = distanceFromCurrentLocation;
                                Log.d(TAG, "in onPostExecute of PositionSyncTask setting distanceNearestEventFromCurrentNotification =  " + distanceNearestEventFromCurrentLocation);
                            }

                        }

                    }

                }
                else {
                    trafficValues.put(TrafficContract.TrafficEntry.COLUMN_DISTANCE, -1.0);
                }

                trafficContentValues[contentValuesIndexCounter++] = trafficValues;
            }

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (trafficContentValues != null && trafficContentValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver trafficContentResolver = context.getContentResolver();

                /* Delete old traffic data because we don't need to keep multiple days' data */
                trafficContentResolver.delete(
                        TrafficContract.TrafficEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new traffic data  ContentProvider */
                trafficContentResolver.bulkInsert(
                        TrafficContract.TrafficEntry.CONTENT_URI,
                        trafficContentValues);

                if (generateNotification)
                {

                    NotificationUtils.sendNotification(context,notificationString) ;
                    SharedPreferences.Editor ed = preferences.edit() ;
                    Date currentDate=new Date();
                    ed.putLong("lastnotificationdate", currentDate.getTime());
                    ed.commit();
                    Log.d(TAG, "Notification sent from in onPostExecute of PositionSyncTask ");


                }
                else
                {
                    NotificationUtils.cancelAllNotifications(context);
                    Log.d(TAG, "Notifications clearedin onPostExecute of PositionSyncTask ");
                }



        }


    }
    else{
            Log.d(TAG, "cursor is empty in onPostExecute of PositionSyncTask");

        }
  }   // end of PostExecute
}
