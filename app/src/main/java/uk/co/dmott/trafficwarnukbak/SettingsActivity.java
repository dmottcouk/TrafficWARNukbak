package uk.co.dmott.trafficwarnukbak;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static boolean trackingSwitchEnabled = false;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            String prefKey = preference.getKey();

            Log.i(TAG, "OnPreferenceChangeListener - key =  " + prefKey);
            Log.i(TAG, "OnPreferenceChangeListener - value =  " + stringValue);


            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }else if (preference.getKey().matches("location_active_switch")) {
                Log.i(TAG, "In Settings Activity and the location tracking preference has been changed ");
                if (trackingSwitchEnabled == false){

                    Log.i(TAG, "In Settings Activity and the location tracking preference can not be activated ");
                    preference.setEnabled(false);
                    return false; // dont update value
                }else
                {
                    Log.i(TAG, "In Settings Activity and the location tracking preference can  be activated ");
                    preference.setEnabled(true);
                }


            }
            else
            {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };





    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(savedInstanceState != null){
            Log.i(TAG, "OnCreate of SettingsActivity - savedInstanceState is not null");
            trackingSwitchEnabled = savedInstanceState.getBoolean("SavedInstancetrackingenabled");
            Log.i(TAG, "OnCreate of SettingsActivity - savedInstanceState shows saved value " + trackingSwitchEnabled );
        }
        else {


            Bundle extras = getIntent().getExtras();
            if (extras.isEmpty()){

                Log.i(TAG, "OnCreate of SettingsActivity - extras is exmpty");

            }


            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences.contains(getResources().getString(R.string.pref_track_location_enabled_key))){
                boolean enableLocationKey = preferences.getBoolean(getResources().getString(R.string.pref_track_location_enabled_key), false);

                if (enableLocationKey)
                {
                    trackingSwitchEnabled = true;
                    Log.i(TAG, "OnCreate of SettingsActivity - tracking switch is enabled because enableLocationKey is true ");

                }
                else
                {
                    trackingSwitchEnabled = false;
                    Log.i(TAG, "OnCreate of SettingsActivity - tracking switch is disabled because enableLocationKey is false ");

                }


            }

            else if ((!extras.isEmpty()) && extras.getBoolean("AllowLocationTracking")) {
                trackingSwitchEnabled = true;
                Log.i(TAG, "OnCreate of SettingsActivity - tracking switch is enabled");
            } else {
                trackingSwitchEnabled = false;
                Log.i(TAG, "OnCreate of SettingsActivity - tracking switch is disabled");
            }



        }


        setupActionBar();
    }


    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean("SavedInstancetrackingenabled", trackingSwitchEnabled );
        Log.i(TAG, "OnCreate onSavedInstanceStateSetting SavedInstancetrackingenabled" + trackingSwitchEnabled);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause of SettingsActivity. trackingswitch = " + trackingSwitchEnabled);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor ed = preferences.edit();

        ed.putBoolean(getResources().getString(R.string.pref_track_location_enabled_key), trackingSwitchEnabled);
        ed.commit();


        Log.i(TAG, "onPause - set the pref_track_location_enabled_key to  " + trackingSwitchEnabled);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                Intent upIntent = new Intent(this, TrafficItemListActivity.class);

                startActivity(upIntent);
                return true;
        }





        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onBackPressed() {
        Log.i(TAG, "Back pressed");

        startActivity(new Intent(this, TrafficItemListActivity.class));
        super.onBackPressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            Preference pref = findPreference(getResources().getString(R.string.pref_track_location_key));
            pref.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_notification_interval_key)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_notification_type_key)));

            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_notification_radius_key)));




        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_traffic_update_interval_key)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_location_priority_type_key)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_current_location_update_interval_key)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}

