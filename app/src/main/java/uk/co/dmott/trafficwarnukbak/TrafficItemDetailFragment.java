package uk.co.dmott.trafficwarnukbak;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.dmott.trafficwarnukbak.data.TrafficContract;

import uk.co.dmott.trafficwarnukbak.sync.TrafficwarnukSyncUtils;

/**
 * A fragment representing a single TrafficItem detail screen.
 * This fragment is either contained in a {@link TrafficItemListActivity}
 * in two-pane mode (on tablets) or a {@link TrafficItemDetailActivity}
 * on handsets.
 */
public class TrafficItemDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_CATEGORY = "item_category";


    private static final int ID_DETAIL_LOADER = 353;

    /*
   * The columns of data that we are interested in displaying within our DetailActivity's
   * traffic display.
   */
    public static final String[] TRAFFIC_DETAIL_PROJECTION = {
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




    private ImageView miconiv;

    private TextView mroadcountytv;

    private TextView mcattv;

    private TextView mtitletv;

    private TextView mdesctv;

    private TextView mdistancetv;

    private String mguid;

    private String mcategory;




    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrafficItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mguid = savedInstanceState.getString(ARG_ITEM_ID);
            mcategory = savedInstanceState.getString(ARG_ITEM_CATEGORY);



            Bundle myBundle = new Bundle();
            myBundle.putString("guid", mguid);

            getLoaderManager().initLoader(ID_DETAIL_LOADER, myBundle, this);


            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                if (mcategory.startsWith("RoadOrCarriageway") )
                    mcategory = "Road Management";
                appBarLayout.setTitle(mcategory);
            }

        }
        else {

            if (getArguments().containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                Bundle myBundle = new Bundle();

                String localGUID = getArguments().getString(ARG_ITEM_ID);
                String localCategory = "RoadNews";

                if (getArguments().containsKey(ARG_ITEM_CATEGORY)) {
                    localCategory = getArguments().getString(ARG_ITEM_CATEGORY);
                }


                mguid = localGUID;
                mcategory = localCategory;


                myBundle.putString("guid", localGUID);

                getLoaderManager().initLoader(ID_DETAIL_LOADER, myBundle, this);


                Activity activity = this.getActivity();
                CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                if (appBarLayout != null) {
                    if (localCategory.startsWith("RoadOrCarriageway") )
                        localCategory = "Road Management";
                    appBarLayout.setTitle(localCategory);
                }


            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trafficitem_detail, container, false);

        miconiv = (ImageView) rootView.findViewById(R.id.detailsCattypeImageView);
        mroadcountytv = (TextView) rootView.findViewById(R.id.detailsRoadCountyTextView);
        mcattv = (TextView) rootView.findViewById(R.id.detailsCategoryTextView);
        mtitletv = (TextView) rootView.findViewById(R.id.detailsTitleTextView);
        mdesctv = (TextView) rootView.findViewById(R.id.detailsDetailTextView);
        mdistancetv = (TextView) rootView.findViewById(R.id.detailsDistanceTextView);


/**
 Activity activity = this.getActivity();
 // Load an ad into the AdMob banner view.
 AdView adView = (AdView) rootView.findViewById(R.id.adView);
 AdRequest adRequest = new AdRequest.Builder()
 .setRequestAgent("android_studio:ad_template").build();
 adView.loadAd(adRequest);
 */

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_ITEM_ID, mguid);
        outState.putString(ARG_ITEM_CATEGORY, mcategory);


        // Always call the superclass so it can save the view hierarchy stat
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {

            case ID_DETAIL_LOADER:

                String selection = TrafficContract.TrafficEntry.COLUMN_GUID + " = ?";
                String selectionarg = args.getString("guid");
                String[] selectionArgs = new String[] { selectionarg };


                return new CursorLoader(this.getActivity(),
                        TrafficContract.TrafficEntry.CONTENT_URI,
                        TRAFFIC_DETAIL_PROJECTION,
                        selection,
                        selectionArgs,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            String cat1 = data.getString(INDEX_TRAFFIC_CATEGORY1);
            String cat2 = data.getString(INDEX_TRAFFIC_CATEGORY2);
            String desc = data.getString(INDEX_TRAFFIC_DESCRIPTION);
            String title = data.getString(INDEX_TRAFFIC_TITLE);
            String road = data.getString(INDEX_TRAFFIC_ROAD);
            String region = data.getString(INDEX_TRAFFIC_REGION);
            String county = data.getString(INDEX_TRAFFIC_COUNTY);
            Double lat = data.getDouble(INDEX_TRAFFIC_LATITUDE);
            Double longit = data.getDouble(INDEX_TRAFFIC_LONGITUDE);
            String link = data.getString(INDEX_TRAFFIC_LINK);
            String pubdate = data.getString(INDEX_TRAFFIC_PUBDATE);
            Double distance = data.getDouble(INDEX_TRAFFIC_DISTANCE); // this is in miles

/* released code has graphics removed
            if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("accident")) {

                miconiv.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("congestion")) {

                miconiv.setImageResource(R.drawable.traffic);
            }else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("obstruction")) {

                miconiv.setImageResource(R.drawable.genobstruction);
            }
            else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("people")) {

                miconiv.setImageResource(R.drawable.peopleonroad);
            }
            else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("overturned")) {

                miconiv.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("spillage")) {

                miconiv.setImageResource(R.drawable.spillage);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("brokendownvehicle")) {

                miconiv.setImageResource(R.drawable.accident);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("flooding")) {

                miconiv.setImageResource(R.drawable.flood);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("barrier/bridgerepairs")) {

                miconiv.setImageResource(R.drawable.flood);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("roadworks")) {

                miconiv.setImageResource(R.drawable.roadworks);
            }  else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("lanemanagement")) {
                miconiv.setImageResource(R.drawable.carraigewaymanagement);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("authority")) {
                miconiv.setImageResource(R.drawable.authorityoperation);
            } else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("fire")) {
                miconiv.setImageResource(R.drawable.vehiclefire);
            }
            else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("recovery")) {
                miconiv.setImageResource(R.drawable.recovery2);
            }
            else if (cat1.toLowerCase().replaceAll("[ \t]", "").contains("animals")) {
                miconiv.setImageResource(R.drawable.animals);
            }
            else
                miconiv.setImageResource(R.drawable.ic_launcher);

**/

            miconiv.setImageResource(R.drawable.ic_launcher);

            mroadcountytv.setText(road + "-" + county + "-" + region);
            mcattv.setText(cat1);
            mtitletv.setText(title);
            mdesctv.setText(desc);


            // need to decide whether we are showing in miles or kilometres
            Context myctxt = getContext();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myctxt);


            boolean locationTrackingEnabled  = sharedPreferences.getBoolean(myctxt.getResources().getString(R.string.pref_track_location_key), myctxt.getResources().getBoolean(R.bool.pref_track_location_default));

            if (locationTrackingEnabled) {


                boolean showInMiles = sharedPreferences.getBoolean(myctxt.getResources().getString(R.string.pref_show_miles_key), myctxt.getResources().getBoolean(R.bool.pref_show_miles_default));

                if (!showInMiles) {
                    String result = String.format("%.2f", TrafficwarnukSyncUtils.convertMilesToKilometers(distance));
                    mdistancetv.setText(result + "  Km");
                } else {
                    String result = String.format("%.2f", distance);
                    mdistancetv.setText(result + "  Mls");

                }
            }
            else
                mdistancetv.setText("Location disabled");

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}