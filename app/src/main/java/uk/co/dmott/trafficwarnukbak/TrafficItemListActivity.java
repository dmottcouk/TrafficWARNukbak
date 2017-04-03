package uk.co.dmott.trafficwarnukbak;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.dmott.trafficwarnukbak.data.TrafficContract;
import uk.co.dmott.trafficwarnukbak.sync.TrafficSyncTask;
import uk.co.dmott.trafficwarnukbak.sync.TrafficwarnukSyncUtils;

import static android.R.attr.defaultValue;
import static uk.co.dmott.trafficwarnukbak.R.id.category2TitleTextViewold;


/**
 * An activity representing a list of TrafficItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TrafficItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TrafficItemListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;


    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private AlarmManager alarmManager;

    /*
       * The columns of data that we are interested in displaying within our MainActivity's list of
       * traffic data.
       */
    public static final String[] MAIN_TRAFFIC_PROJECTION = {
            TrafficContract.TrafficEntry.COLUMN_CATEGORY1,
            TrafficContract.TrafficEntry.COLUMN_CATEGORY2,
            TrafficContract.TrafficEntry.COLUMN_DESCRIPTION,
            TrafficContract.TrafficEntry.COLUMN_TITLE,
            TrafficContract.TrafficEntry.COLUMN_ROAD,
            TrafficContract.TrafficEntry.COLUMN_REGION,
            TrafficContract.TrafficEntry.COLUMN_COUNTY,
            TrafficContract.TrafficEntry.COLUMN_LATITUDE,
            TrafficContract.TrafficEntry.COLUMN_LONGITUDE,
            TrafficContract.TrafficEntry.COLUMN_OVERALLSTART,
            TrafficContract.TrafficEntry.COLUMN_OVERALLEND,
            TrafficContract.TrafficEntry.COLUMN_EVENTSTART,
            TrafficContract.TrafficEntry.COLUMN_EVENTEND,
            TrafficContract.TrafficEntry.COLUMN_LINK,
            TrafficContract.TrafficEntry.COLUMN_PUBDATE,
            TrafficContract.TrafficEntry.COLUMN_STOREDATE,
            TrafficContract.TrafficEntry.COLUMN_REFERENCE,
            TrafficContract.TrafficEntry.COLUMN_GUID,
            TrafficContract.TrafficEntry.COLUMN_AUTHOR,
            TrafficContract.TrafficEntry.COLUMN_DISTANCE

    };

    private final String TAG = TrafficItemListActivity.class.getSimpleName();

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_TRAFFIC_CATEGORY1 = 0;
    public static final int INDEX_TRAFFIC_CATEGORY2 = 1;
    public static final int INDEX_TRAFFIC_DESCRIPTION = 2;
    public static final int INDEX_TRAFFIC_TITLE = 3;
    public static final int INDEX_TRAFFIC_ROAD = 4;
    public static final int INDEX_TRAFFIC_REGION = 5;
    public static final int INDEX_TRAFFIC_COUNTY = 6;
    public static final int INDEX_TRAFFIC_LATITUDE = 7;
    public static final int INDEX_TRAFFIC_LONGITUDE = 8;
    public static final int INDEX_TRAFFIC_OVERALLSTART = 9;
    public static final int INDEX_TRAFFIC_OVERALLEND = 10;
    public static final int INDEX_TRAFFIC_EVENTSTART = 11;
    public static final int INDEX_TRAFFIC_EVENTEND = 12;


    public static final int INDEX_TRAFFIC_LINK = 13;
    public static final int INDEX_TRAFFIC_PUBDATE = 14;
    public static final int INDEX_TRAFFIC_STOREDATE = 15;
    public static final int INDEX_TRAFFIC_REFERENCE = 16;
    public static final int INDEX_TRAFFIC_GUID = 17;
    public static final int INDEX_TRAFFIC_AUTHOR = 18;
    public static final int INDEX_TRAFFIC_DISTANCE = 19;




    /*
        * This ID will be used to identify the Loader responsible for loading our traffic. In
        * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
        * We will still use this ID to initialize the loader and create the loader for best practice.
        * Please note that 44 was chosen arbitrarily. You can use whatever number you like, so long as
        * it is unique and consistent.
        */
    private static final int ID_TRAFFIC_LOADER = 44;

    private static final int PERMISSION_REQUEST_LOCATION = 0;

    private TrafficNewAdapter mTrafficAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private ProgressBar mLoadingIndicator;
    protected PendingIntent locationPendingIntent;
    protected Location mCurrentLocation;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    protected LocationRequest mLocationRequest;

    public static final String MyPREFERENCES = "MyLocation" ;


    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private boolean mLocationMonitoringEnabled;
    private boolean mInitialized = false;
    public Context Ctxt;
    private boolean mShowingDistance;


    @Override
    public void onBackPressed() {
        //android.os.process.killProcess(android.os.process.myPid());
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trafficitem_list);
        mInitialized = false;
        mShowingDistance = false;
        Ctxt = getBaseContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String locUpdated;
                String trafficUpdated;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                if(preferences.contains("positionobtaineddate")) {

                    long lastLocationSyncTimeAsLong = preferences.getLong("positionobtaineddate", Double.doubleToLongBits(defaultValue));
                    Date lastLocSyncTime = new Date();
                    lastLocSyncTime.setTime(lastLocationSyncTimeAsLong);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy kk:mm");
                    locUpdated = sdf.format(lastLocSyncTime);
                }
                else
                {
                    locUpdated = "Unknown";
                }

                if(preferences.contains("trafficobtaineddate")) {

                    long lastTrafficSyncTimeAsLong = preferences.getLong("trafficobtaineddate", Double.doubleToLongBits(defaultValue));
                    Date lastTrafficSyncTime = new Date();
                    lastTrafficSyncTime.setTime(lastTrafficSyncTimeAsLong);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy kk:mm");
                    trafficUpdated = sdf.format(lastTrafficSyncTime);
                }
                else
                {
                    trafficUpdated = "Unknown";
                }

                if (mLocationMonitoringEnabled) {


                    Snackbar snackbar = Snackbar.make(view, "Last Location Sync : " + locUpdated + " \nLast traffic Sync : " + trafficUpdated,
                            Snackbar.LENGTH_LONG).setDuration(Snackbar.LENGTH_LONG);

                    View snackbarView = snackbar.getView();
                    TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

                    tv.setMaxLines(3);
                    snackbar.setAction("Action", null).show();
                } else
                {
                    Snackbar snackbar = Snackbar.make(view, "Last Location Sync : Location disabled" + " \nLast traffic Sync : " + trafficUpdated,
                            Snackbar.LENGTH_LONG).setDuration(Snackbar.LENGTH_LONG);

                    View snackbarView = snackbar.getView();
                    TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

                    tv.setMaxLines(3);
                    snackbar.setAction("Action", null).show();




                }

                if(mShowingDistance == false)
                {

                    // real Nexus 5x does not like this in debug mode!!!!!!

                    AsyncTask<Void,Void,Void> mFetchTrafficTask = new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Context context = getApplicationContext();
                            TrafficSyncTask.syncTraffic(context);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {

                        }
                    };

                    mFetchTrafficTask.execute();

                }

            }
        });

        View recyclerView = findViewById(R.id.trafficitem_list);
        assert recyclerView != null;


        if (findViewById(R.id.trafficitem_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        //setupSharedPreferences();
        // find out if we are going to monitor location . At the moment just check the permissions
        mLocationMonitoringEnabled = setupPermissions();



        if (mLocationMonitoringEnabled)  // new way of doing the background location tracking
        {

            cancelAlarmManager();
            startAlarmManager();  //should we call this here ?

        }


        setupRecyclerView((RecyclerView) recyclerView);

        getSupportLoaderManager().initLoader(ID_TRAFFIC_LOADER, null, this);


        TrafficwarnukSyncUtils.initialize(this);



    }

    @Override
    protected void onPostResume() {


        if (mInitialized) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


            boolean locationTrackingEnabled = sharedPreferences.getBoolean(getResources().getString(R.string.pref_track_location_key), getResources().getBoolean(R.bool.pref_track_location_default));
            if (locationTrackingEnabled) {

                Toast.makeText(this, "Location tracking is enabled.", Toast.LENGTH_LONG).show();

                mLocationMonitoringEnabled = true;

            } else {
                Toast.makeText(this, "Location tracking is disabled.", Toast.LENGTH_LONG).show();

                mLocationMonitoringEnabled = false;

            }

            if (mLocationMonitoringEnabled)  // new way of doing the background location tracking
            {

                cancelAlarmManager();
                startAlarmManager();  //should we call this here ?
                Log.i(TAG, "In onPostResume tracking is enabled so start tracking ");



            } else {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor ed = preferences.edit();
                Date currentDate = new Date();

                ed.putLong("longitude", Double.doubleToRawLongBits(-999.0));
                ed.putLong("latitude", Double.doubleToRawLongBits(-999.0));
                ed.putLong("positionobtaineddate", currentDate.getTime());

                ed.commit();
                cancelAlarmManager();
                Log.i(TAG, "In onPostResume tracking is disabled so set location to -999 ");

            }

            View recyclerView = findViewById(R.id.trafficitem_list);
            assert recyclerView != null;
            setupRecyclerView((RecyclerView) recyclerView);

            getSupportLoaderManager().initLoader(ID_TRAFFIC_LOADER, null, this);


            TrafficwarnukSyncUtils.initialize(this);
        }
        else
        {
            mInitialized = true;
        }

        super.onPostResume();
    }


    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager");

        Context context = getBaseContext();
        Intent gpsTrackerIntent = new Intent(context, LocationTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1245, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }



    private void startAlarmManager() {
        Log.d(TAG, "startAlarmManager");

        Context context = getBaseContext();

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent locationTrackerIntent = new Intent(context, LocationTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1245, locationTrackerIntent, 0);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Long updateInterval = Long.parseLong(preferences.getString(getResources().getString(R.string.pref_current_location_update_interval_key), "300"));

        Log.d(TAG, "startAlarmManager : Setting alarm to " + updateInterval + "seconds");


        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                //                 UPDATE_INTERVAL_IN_MILLISECONDS, // 60000 = 1 minute
                updateInterval * 1000,
                pendingIntent);
        //  }
    }







    @Override
    protected void onStart() {

        super.onStart();

    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        ifTTsServiceRunningStopIt(TTService.class);

        super.onDestroy();
    }

    private void ifTTsServiceRunningStopIt(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Intent stopIntent = new Intent(this, TTService.class);;
                stopService(stopIntent);
            }
        }

    }



    private boolean setupPermissions()
    {
        // If we don't have the location permission...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
                requestPermissions(permissionsWeNeed, PERMISSION_REQUEST_LOCATION);
            }
            return false;
        } else {
            // Otherwise, permissions were granted and we are ready to go if the preference allows!


            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


            boolean locationTrackingEnabled  = sharedPreferences.getBoolean(getResources().getString(R.string.pref_track_location_key), getResources().getBoolean(R.bool.pref_track_location_default));
            if (locationTrackingEnabled) {

                Toast.makeText(this, "Location tracking is enabled.", Toast.LENGTH_LONG).show();

                mLocationMonitoringEnabled = true;
                return true;
            }
            else
            {
                Toast.makeText(this, "Location tracking is disabled.", Toast.LENGTH_LONG).show();

                mLocationMonitoringEnabled = false;
                return false;
            }



        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


                    boolean locationTrackingEnabled  = sharedPreferences.getBoolean(getResources().getString(R.string.pref_track_location_key), getResources().getBoolean(R.bool.pref_track_location_default));
                    if (locationTrackingEnabled) {

                        mLocationMonitoringEnabled = true;

                        Toast.makeText(this, "Location tracking is enabled.", Toast.LENGTH_LONG).show();
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//                    SharedPreferences.Editor editor = preferences.edit();
//                    if (!(preferences.contains("currentlytracking")) ||   (preferences.getBoolean("currentlytracking", false) == false    ))
                        //                   {
                        cancelAlarmManager();
                        startAlarmManager();
                    }
                    else
                    {

                        mLocationMonitoringEnabled = false;
                        Toast.makeText(this, "Permission for location granted but option is disabled so am enabling it.", Toast.LENGTH_LONG).show();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                        SharedPreferences.Editor ed = preferences.edit();

                        ed.putBoolean(getResources().getString(R.string.pref_track_location_key), true);
                        ed.putBoolean(getResources().getString(R.string.pref_track_location_enabled_key), true);
                        ed.commit();



                    }


                } else {
                    Toast.makeText(this, "Permission for location not granted. This functionality is disabled.", Toast.LENGTH_LONG).show();
                    //finish();
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                    SharedPreferences.Editor ed = preferences.edit();

                    ed.putBoolean(getResources().getString(R.string.pref_track_location_key), false);
                    ed.commit();


                    mLocationMonitoringEnabled = false;
                }
            }
            // Other permissions could go down here

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {





            Bundle myBundle = new Bundle();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            SharedPreferences.Editor ed = preferences.edit();


            int permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);



            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission to access location denied so set Bundle value to false before calling SettingsActivity");
                Log.i(TAG, "Also setting pref_track_location_enabled_key to  false before calling SettingsActivity");
                myBundle.putBoolean("AllowLocationTracking", false);
                ed.putBoolean(getResources().getString(R.string.pref_track_location_enabled_key), false);
            }else
            {
                Log.i(TAG, "Permission to access location allowed so set Bundle value to true before calling SettingsActivity");
                Log.i(TAG, "Also setting pref_track_location_enabled_key to  true before calling SettingsActivity");
                myBundle.putBoolean("AllowLocationTracking", true);
                ed.putBoolean(getResources().getString(R.string.pref_track_location_enabled_key), true);
            }

            ed.commit();




            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startSettingsActivity.putExtras(myBundle);

            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }




    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
        mRecyclerView = (RecyclerView) findViewById(R.id.trafficitem_list);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        //mTrafficAdapter = new TrafficAdapter(this, this, mTwoPane);
        mTrafficAdapter = new TrafficNewAdapter(this);



        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mTrafficAdapter);


    }

    public class TrafficNewAdapter
            extends RecyclerView.Adapter<TrafficNewAdapter.ViewHolder> {

        /* The context we use to utility methods, app resources and layout inflaters */
        private final Context mContext;

        private Cursor mCursor;
        private double mAdapterLongitude;
        private double mAdapterLatitude;
        private boolean mshowDistanceInMiles = true;

        public void setAdapterLongitude(double longitude)
        {
            mAdapterLongitude = longitude;

        }
        public void setAdapterLatitude(double latitude)
        {
            mAdapterLatitude = latitude;

        }

        public void setAdapterShowDistanceInMiles(boolean inMiles)
        {
            mshowDistanceInMiles = inMiles;

        }

        public TrafficNewAdapter(@NonNull Context context) {
            mContext = context;


        }


        @Override
        public TrafficNewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //layoutId = R.layout.traffic_list_item_constraint;
            int layoutId;

            layoutId = R.layout.traffic_list_item;


            View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

            view.setFocusable(true);
            //view.setBackgroundColor(rgb(255,255,0));

            return new TrafficNewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TrafficNewAdapter.ViewHolder trafficAdapterViewHolder, int position) {
            mCursor.moveToPosition(position);

            /**
             There is a trick to saving the database Id somewhere in the UI in a recyclerview
             RecyclerView offers a Tag Object
             */


            // long id = mCursor.getLong(mCursor.getColumnIndex(TrafficContract.TrafficEntry._ID));

            // trafficAdapterViewHolder.itemView.setTag(id);

            //SharedPreferences.Editor ed;
            //double currentLatitude = -1.0;
            //double currentLongitude = -1.0;

            String cat1 = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_CATEGORY1);
            String road = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_ROAD);
            String region = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_REGION);
            String county = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_COUNTY);
            String cat2 = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_CATEGORY2);
            String title = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_TITLE);



            //get the distance from current location . This is in miles
            double distanceFromCurrentLocation = mCursor.getDouble(TrafficItemListActivity.INDEX_TRAFFIC_DISTANCE);

            if (distanceFromCurrentLocation > 0)
                mShowingDistance = true;

            if (mshowDistanceInMiles == false)
                distanceFromCurrentLocation = TrafficwarnukSyncUtils.convertMilesToKilometers(distanceFromCurrentLocation);


            String formatteddistanceFRomCurrentLocation = String.format("%.2f", distanceFromCurrentLocation);

/**

            if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("accident")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("overturnedvehicle")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("spillage")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.spillage);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("obstruction")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.genobstruction);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("congestion")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.traffic);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("brokendownvehicle")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("people")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.peopleonroad);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("flooding")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.flood);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("barrier/bridgerepairs")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("roadworks")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.roadworks);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("authority")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.authorityoperation);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("lanemanagement")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.carraigewaymanagement);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("recovery")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.recovery2);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("fire")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.vehiclefire);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("animals")) {
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.animals);
            } else
                trafficAdapterViewHolder.iconView.setImageResource(R.drawable.ic_launcher);
**/

            trafficAdapterViewHolder.iconView.setImageResource(R.drawable.ic_launcher);

            trafficAdapterViewHolder.roadcountytv.setText(road + "/" + region + "/" + county);
            trafficAdapterViewHolder.titletv.setText(cat2 + "/" + title);

            //calculatedistancefromCurrentLocation


            if (mshowDistanceInMiles)
                trafficAdapterViewHolder.distancetv.setText(formatteddistanceFRomCurrentLocation + " Miles");
            else
                trafficAdapterViewHolder.distancetv.setText(formatteddistanceFRomCurrentLocation + " Kilometers");


        }

        @Override
        public int getItemCount() {
            if (null == mCursor) return 0;
            return mCursor.getCount();
        }

        void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            //System.out.println("Cursor now has" + mCursor.getCount());
            notifyDataSetChanged();
        }

        /**
         * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
         * a cache of the child views for a traffic item. It's also a convenient place to set an
         * OnClickListener, since it has access to the adapter and the views.
         */
        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView iconView;

            final TextView roadcountytv;
            final TextView titletv;
            final TextView distancetv;


            ViewHolder(View view) {
                super(view);

                //iconView = (ImageView) view.findViewById(R.id.catTypeImageView);
                //roadcountytv = (TextView) view.findViewById(R.id.roadCountyTextView);
                //titletv = (TextView) view.findViewById(R.id.category2TitleTextView);

                iconView = (ImageView) view.findViewById(R.id.catTypeImageViewold);
                roadcountytv = (TextView) view.findViewById(R.id.roadCountyTextViewold);
                titletv = (TextView) view.findViewById(category2TitleTextViewold);
                distancetv = (TextView) view.findViewById(R.id.distanceTextViewold);


                view.setOnClickListener(this);
            }

            /**
             * This gets called by the child views during a click. We fetch the date that has been
             * selected, and then call the onClick handler registered with this adapter, passing that
             * date.
             *
             * @param v the View that was clicked
             */
            @Override
            public void onClick(View v) {
                int adapterPosition = getAdapterPosition();
                mCursor.moveToPosition(adapterPosition);


                String guid = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_GUID);

                String category = mCursor.getString(TrafficItemListActivity.INDEX_TRAFFIC_CATEGORY1);



                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(TrafficItemDetailFragment.ARG_ITEM_ID, guid);
                    arguments.putString(TrafficItemDetailFragment.ARG_ITEM_CATEGORY, category);

                    TrafficItemDetailFragment fragment = new TrafficItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.trafficitem_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, TrafficItemDetailActivity.class);
                    intent.putExtra(TrafficItemDetailFragment.ARG_ITEM_ID, guid);

                    intent.putExtra(TrafficItemDetailFragment.ARG_ITEM_CATEGORY, category);
                    context.startActivity(intent);
                }
            }
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {


        switch (loaderId) {

            case ID_TRAFFIC_LOADER:
                /* URI for all rows of traffic  data in our weather table */
                Uri forecastQueryUri = TrafficContract.TrafficEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                // this is where the order for recyclerview is defined
                //               String sortOrder = TrafficContract.TrafficEntry.COLUMN_PUBDATE + " DESC";

                String sortOrder = "";
               // if (mLocationMonitoringEnabled) {
                    sortOrder = TrafficContract.TrafficEntry.COLUMN_DISTANCE + " ASC";
                //    Log.i(TAG, "In onCreateLoader - set order to DISTANCE");
               // }else
               // {
               //     sortOrder = TrafficContract.TrafficEntry.COLUMN_CATEGORY1 + " ASC";
               //     Log.i(TAG, "In onCreateLoader - set order to CATEGORY");
               // }
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all traffic data from today onwards that is stored in our traffic table.
                 */
                //String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_TRAFFIC_PROJECTION,
                        null,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }
    /**
     * Called when a Loader has finished loading its data.
     *
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
/**
 if(!preferences.contains("longitude")){
 Log.d(TAG, "There is no defaultpreference value for longitude");
 }
 else
 {
 mCurrentLongitude = Double.longBitsToDouble(preferences.getLong("longitude", Double.doubleToLongBits(defaultValue)));
 String formattedCurrentLongitude = String.format("%.2f", mCurrentLongitude);
 Log.d(TAG, "current longitude in adapter is" + formattedCurrentLongitude);
 mTrafficAdapter.setAdapterLongitude(mCurrentLongitude);

 }
 if(!preferences.contains("latitude")){
 Log.d(TAG, "There is no defaultpreference value for latitude");
 }
 else
 {
 mCurrentLatitude = Double.longBitsToDouble(preferences.getLong("latitude", Double.doubleToLongBits(defaultValue)));
 String formattedCurrentLatitude = String.format("%.2f", mCurrentLatitude);
 Log.d(TAG, "current latitude in adapter is" + formattedCurrentLatitude);
 mTrafficAdapter.setAdapterLatitude(mCurrentLatitude);
 }
 */

        if(!preferences.contains(getResources().getString(R.string.pref_show_miles_key))){
            Log.d(TAG, "There is no defaultpreference value for show_miles");
        }
        else
        {
            boolean shMiles = preferences.getBoolean(getResources().getString(R.string.pref_show_miles_key), true);
            mTrafficAdapter.setAdapterShowDistanceInMiles(preferences.getBoolean(getResources().getString(R.string.pref_show_miles_key), true)) ;



        }

        //setAdapterShowDistanceInMiles



        mTrafficAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);

    }
    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mTrafficAdapter.swapCursor(null);
    }

}
