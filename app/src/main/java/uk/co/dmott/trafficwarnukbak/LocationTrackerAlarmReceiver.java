package uk.co.dmott.trafficwarnukbak;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by david on 05/03/17.
 */

public class LocationTrackerAlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "LocationTrackerAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LocationService.class));
    }
}
