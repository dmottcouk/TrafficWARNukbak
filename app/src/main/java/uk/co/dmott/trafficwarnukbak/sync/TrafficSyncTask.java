package uk.co.dmott.trafficwarnukbak.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.co.dmott.trafficwarnukbak.R;
import uk.co.dmott.trafficwarnukbak.TTService;
import uk.co.dmott.trafficwarnukbak.TrafficItemListActivity;
import uk.co.dmott.trafficwarnukbak.data.TrafficContract;
import uk.co.dmott.trafficwarnukbak.data.TrafficPojoEntry;
import uk.co.dmott.trafficwarnukbak.utilities.NetworkUtils;
import uk.co.dmott.trafficwarnukbak.utilities.TrafficXmlUtils;

import static android.R.attr.defaultValue;

/**
 * Created by david on 21/03/17.
 */

public class TrafficSyncTask {

    private static final String TAG = TrafficSyncTask.class.getSimpleName();

    private static double sCurrentLongitude = -999;
    private static double sCurrentLatitude = -999;
    private static Date sposObtainedDate = null;

    /**
     * A numeric value that identifies the notification that we'll be sending.
     * This value needs to be unique within this app, but it doesn't need to be
     * unique system-wide.
     */
    public static final int NOTIFICATION_ID = 1;


    synchronized public static void syncTraffic(Context context) {


            /*
             * The getUrl method will return the URL that we need to get the traffic XML for the
             * unplanned incidents.
             */
        try {

            List<TrafficPojoEntry> trafficItems;
            Double distanceFromCurrentLocation = -1.0; // this is always in miles
            Double distanceNearestEventFromCurrentLocation = -1.0; // this is always in miles
            char distanceUnits = 'M';
            boolean generateNotification = false; // dont generate notification by default
            String notificationString = "";
            boolean notificationDistanceInMiles = true;
            boolean preventNotification = false;


            int contentValuesIndexCounter = 0;
            int notificationRadius = 0;
            int notificationInterval = 0;



            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if(!preferences.contains("longitude")){
                Log.d(TAG, "There is no defaultpreference value for longitude");
            }
            else
            {
                sCurrentLongitude = Double.longBitsToDouble(preferences.getLong("longitude", Double.doubleToLongBits(defaultValue)));
                String formattedCurrentLongitude = String.format("%.2f", sCurrentLongitude);
                Log.d(TAG, "current longitude in synctask is" + formattedCurrentLongitude);


            }
            if(!preferences.contains("latitude")){
                Log.d(TAG, "There is no defaultpreference value for latitude");
            }
            else
            {
                sCurrentLatitude = Double.longBitsToDouble(preferences.getLong("latitude", Double.doubleToLongBits(defaultValue)));
                String formattedCurrentLatitude = String.format("%.2f", sCurrentLatitude);
                Log.d(TAG, "current latitude in synctask is" + formattedCurrentLatitude);

            }

            if(!preferences.contains("positionobtaineddate")){
                Log.d(TAG, "There is no defaultpreference value for obtained date");

            }
            else
            {
                long millis = preferences.getLong("positionobtaineddate", 0L);
                sposObtainedDate = new Date(millis);

                Log.d(TAG, "position obtained date  in synctask is" + sposObtainedDate.toString());




            }
            if(!preferences.contains("show_miles")){
                Log.d(TAG, "There is no defaultpreference value for preferred distance units");
            }
            else
            {
                notificationDistanceInMiles = preferences.getBoolean("show_miles", true);


                Log.d(TAG, "preferred distance units obtained in TrafficSyncTask ");

            }


            notificationInterval  =   Integer.parseInt(preferences.getString(context.getResources().getString(R.string.pref_notification_interval_key), "600"))/ 60;
            Log.d(TAG, "Notification interval preference in mins  =  " + notificationInterval);




            if(!preferences.contains("lastnotificationdate")){
                Log.d(TAG, "There is no defaultpreference value for lastnotificationdate");
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
                    Log.d(TAG, "set prevent notification as not passed interval = " + notificationInterval + "minutes");
                }
                else {
                    preventNotification = false;
                    Log.d(TAG, "clear prevent notification as passed interval = " + notificationInterval + "minutes");
                }

            }

            if(!preferences.contains(context.getResources().getString(R.string.pref_notification_radius_key))){
                Log.d(TAG, "There is no defaultpreference value for pref_notification_radius");
            }
            else
            {
                String prefRadius= preferences.getString(context.getResources().getString(R.string.pref_notification_radius_key), "10");
            }


            notificationRadius  =   Integer.parseInt(preferences.getString(context.getResources().getString(R.string.pref_notification_radius_key), "10"));
            Log.d(TAG, "Notification radius preference =  " + notificationRadius);
            URL trafficRequestUrl = NetworkUtils.getUnplannedUrl();


            distanceUnits = 'M';



            trafficItems = TrafficXmlUtils.getTrafficDataFromXML(); // Get the XML items from the internet into trafficItems

            ContentValues[] trafficContentValues = new ContentValues[trafficItems.size()];


            for(TrafficPojoEntry ent: trafficItems) {
                ContentValues trafficValues = new ContentValues();
                Calendar now = Calendar.getInstance();
                Date dateNow = now.getTime();


                distanceFromCurrentLocation = -1.0;


                if ((sCurrentLatitude != -999) && (sCurrentLongitude != -999))
                {
                    distanceFromCurrentLocation = TrafficwarnukSyncUtils.calculateDistanceFromCurrentLocation(sCurrentLatitude, sCurrentLongitude,ent.getLatitude(),ent.getLongitude(),distanceUnits);


                }


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

                if (distanceFromCurrentLocation >= 0)
                {
                    trafficValues.put(TrafficContract.TrafficEntry.COLUMN_DISTANCE, distanceFromCurrentLocation);
                    if ((distanceFromCurrentLocation < notificationRadius) &&  (preventNotification == false)) // if less than notificationRadius miles away send notification

     /* FRIG */

  //                  if ((distanceFromCurrentLocation < 70.0) &&  (preventNotification == false)) // if less than notificationRadius miles away send notification
                    {
                        //check if there is any evemt within notificationRadius miles of current location
                        //if there is generate a notification to the watch

                        String catg = ent.getCategory1();

                        Log.d(TAG, "Event is closer than the notificationRadius which is " + notificationRadius);


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



            /* If the code reaches this point, we have successfully performed our sync */
                // store the traffic sync time in sharedpreferences

                SharedPreferences.Editor ed = preferences.edit() ;
                Date currentDate=new Date();
                ed.putLong("trafficobtaineddate", currentDate.getTime());
                ed.commit();


            }
            if (generateNotification)
            {

                sendNotification(context,notificationString) ;
                SharedPreferences.Editor ed = preferences.edit() ;
                Date currentDate=new Date();
                ed.putLong("lastnotificationdate", currentDate.getTime());
                ed.commit();
                Log.d(TAG, "Notification sent ");


            }
            else
            {
                cancelAllNotifications(context);
                Log.d(TAG, "Notifications cleared ");
            }


        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }


    }

    private static void cancelAllNotifications(Context context) {

        NotificationManager notifManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }


    public static void sendNotification(Context ctxt, String page2String) {

        // BEGIN_INCLUDE(build_action)
        /** Create an intent that will be fired when the user clicks the notification.
         * The intent needs to be packaged into a {@link android.app.PendingIntent} so that the
         * notification service can fire it on our behalf.
         */

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctxt);


        int notificationType  =   Integer.parseInt(sharedPreferences.getString(ctxt.getResources().getString(R.string.pref_notification_type_key), "2"));



        boolean ttsFlag  =   sharedPreferences.getBoolean(ctxt.getResources().getString(R.string.pref_tts_key), false);
        Log.d(TAG, "sendNotification : Send TTS flag = " + ttsFlag);

        if (ttsFlag)
        {
            Intent ttsIntent = new Intent(ctxt, TTService.class);
            ttsIntent.putExtra("textToSpeak", "Notification from TrafficWarn UK. " + page2String);
            Log.d(TAG, "sendNotification : Send notification by TTS " + page2String);
            ctxt.startService(ttsIntent);
        }



        Log.d(TAG, "sendNotification : Notification type = " + notificationType);
        if (notificationType > 4) // if no notification then get out
            return;

        //ctxt.stopService(ttsIntent);

        //Intent intentToSendTTSImmediately = new Intent(ctxt, TrafficTTSservice.class);
       // intentToSendTTSImmediately.putExtra("")
        //Log.d(TAG, "sendNotification : Start the TrafficTTSservice service");
        //TrafficTTSservice myTTS = new TrafficTTSservice();
        //myTTS.startActionTTS(ctxt, page2String);

        /* set up pending intent for the watch notification */

        Intent roadnewsIntent = new Intent(ctxt, TrafficItemListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctxt, 0, roadnewsIntent, 0);
        // END_INCLUDE(build_action)

        // BEGIN_INCLUDE (build_notification)
        /**
         * Use NotificationCompat.Builder to set up our notification.
         */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctxt);

        /** Set the icon that will appear in the notification bar. This icon also appears
         * in the lower right hand corner of the notification itself.
         *
         * Important note: although you can use any drawable as the small icon, Android
         * design guidelines state that the icon should be simple and monochrome. Full-color
         * bitmaps or busy images don't render well on smaller screens and can end up
         * confusing the user.
         */
        builder.setSmallIcon(R.drawable.ic_stat_notification);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Set the notification to auto-cancel. This means that the notification will disappear
        // after the user taps it, rather than remaining until it's explicitly dismissed.
        builder.setAutoCancel(true);

        /**
         *Build the notification's appearance.
         * Set the large icon, which appears on the left of the notification. In this
         * sample we'll set the large icon to be the same as our app icon. The app icon is a
         * reasonable default if you don't have anything more compelling to use as an icon.
         */
        builder.setLargeIcon(BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher));

        /**
         * Set the text of the notification. This sample sets the three most commononly used
         * text areas:
         * 1. The content title, which appears in large type at the top of the notification
         * 2. The content text, which appears in smaller text below the title
         * 3. The subtext, which appears under the text on newer devices. Devices running
         *    versions of Android prior to 4.2 will ignore this field, so don't use it for
         *    anything vital!
         */
        builder.setContentTitle("TrafficWarn");
        builder.setContentText("Alert from TrafficWarnUK!");
        builder.setSubText("Tap to open the TrafficWarnUK app on Phone.");

        switch (notificationType){
            case 1:
                builder.setDefaults(Notification.DEFAULT_LIGHTS);
                Log.d(TAG, "sendNotification : Doing a Notification type = DEFAULT_LIGHTS");
                break;
            case 2:
                builder.setDefaults(Notification.DEFAULT_ALL);
                Log.d(TAG, "sendNotification : Doing a Notification type = DEFAULT_ALL");
                break;
            case 3:
                builder.setDefaults(Notification.DEFAULT_SOUND);
                Log.d(TAG, "sendNotification : Doing a Notification type = DEFAULT_SOUND");
                break;
            case 4:
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
                Log.d(TAG, "sendNotification : Doing a Notification type = DEFAULT_VIBRATE");
                break;

        }



        // END_INCLUDE (build_notification)

        // BEGIN_INCLUDE(send_notification)
        /**
         * Send the notification. This will immediately display the notification icon in the
         * notification bar.
         */
        //NotificationManager notificationManager = (NotificationManager) getSystemService(
        //       NOTIFICATION_SERVICE);


        // add a second page to the notification

        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle("Traffic information")
                .bigText(page2String);


        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(ctxt)
                        .setStyle(secondPageStyle)
                        .build();


        builder.extend(new NotificationCompat.WearableExtender()
                .addPage(secondPageNotification))
                .build();



        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctxt.getApplicationContext());


        notificationManager.notify(NOTIFICATION_ID, builder.build());
        // END_INCLUDE(send_notification)
    }


}
