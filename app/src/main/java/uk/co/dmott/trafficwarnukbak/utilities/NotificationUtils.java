package uk.co.dmott.trafficwarnukbak.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import uk.co.dmott.trafficwarnukbak.R;
import uk.co.dmott.trafficwarnukbak.TTService;
import uk.co.dmott.trafficwarnukbak.TrafficItemListActivity;

import static uk.co.dmott.trafficwarnukbak.sync.TrafficSyncTask.NOTIFICATION_ID;

/**
 * Created by david on 02/04/17.
 */

public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    public static synchronized  void sendNotification(Context ctxt, String page2String) {

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


    public static void cancelAllNotifications(Context context) {

        NotificationManager notifManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }

}
