package uk.co.dmott.trafficwarnukbak.sync;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by david on 21/03/17.
 */

public class TrafficwarnukFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mFetchTrafficTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mFetchTrafficTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                TrafficSyncTask.syncTraffic(context);
                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mFetchTrafficTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }




}
