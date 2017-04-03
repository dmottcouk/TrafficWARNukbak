package uk.co.dmott.trafficwarnukbak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.defaultValue;

/**
 * An activity representing a single TrafficItem detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TrafficItemListActivity}.
 */
public class TrafficItemDetailActivity extends AppCompatActivity {

    private final String TAG = TrafficItemDetailActivity.class.getSimpleName();

    @Override
    public void onBackPressed() {
        Log.i(TAG, "Back pressed");
        //NavUtils.navigateUpTo(this, new Intent(this, TrafficItemListActivity.class));
        Intent upIntent = new Intent(this, TrafficItemListActivity.class);
        startActivity(upIntent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trafficitem_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

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


                Snackbar snackbar =  Snackbar.make(view, "Last Location Sync : " + locUpdated + " \nLast traffic Sync : " + trafficUpdated,
                        Snackbar.LENGTH_LONG).setDuration(Snackbar.LENGTH_LONG);

                View snackbarView = snackbar.getView();
                TextView tv= (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

                tv.setMaxLines(3);
                snackbar.setAction("Action", null).show();

            }
        });


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TrafficItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(TrafficItemDetailFragment.ARG_ITEM_ID));
            arguments.putString(TrafficItemDetailFragment.ARG_ITEM_CATEGORY,
                    getIntent().getStringExtra(TrafficItemDetailFragment.ARG_ITEM_CATEGORY));
            TrafficItemDetailFragment fragment = new TrafficItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.trafficitem_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            //NavUtils.navigateUpTo(this, new Intent(this, TrafficItemListActivity.class));
            Intent upIntent = new Intent(this, TrafficItemListActivity.class);
            startActivity(upIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}