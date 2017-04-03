package uk.co.dmott.trafficwarnukbak.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by david on 21/03/17.
 */

public class TrafficSyncIntentService extends IntentService {

    private final static String TAG = TrafficSyncIntentService.class.getSimpleName();

    public TrafficSyncIntentService() {
        super("TrafficSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.i(TAG, "In TrafficSyncIntentService - onHandleIntent ");


        TrafficSyncTask.syncTraffic(this);
    }








}
