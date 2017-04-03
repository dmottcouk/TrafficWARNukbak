package uk.co.dmott.trafficwarnukbak.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by david on 21/03/17.
 */

public class TrafficContract {

    public static final String CONTENT_AUTHORITY = "uk.co.dmott.android.trafficwarnukbak";

    /*
* Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
* the content provider for roadnews.
*/
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TRAFFIC = "traffic";
    /* Inner class that defines the table contents of the traffic table */
    public static final class TrafficEntry implements BaseColumns {
        /* The base CONTENT_URI used to query the Traffic table from the content provider */

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAFFIC)
                .build();

        /* Used internally as the name of our traffic table. */
        public static final String TABLE_NAME = "traffic";

        public static final String COLUMN_CATEGORY1 = "cat1";
        public static final String COLUMN_CATEGORY2 = "cat2";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ROAD = "road";
        public static final String COLUMN_REGION = "region";
        public static final String COLUMN_COUNTY = "county";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_OVERALLSTART = "overallstart";
        public static final String COLUMN_OVERALLEND = "overallend";
        public static final String COLUMN_EVENTSTART = "eventstart";
        public static final String COLUMN_EVENTEND = "eventend";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_PUBDATE = "publisheddate";
        public static final String COLUMN_STOREDATE = "storeddate";
        public static final String COLUMN_REFERENCE = "reference";
        public static final String COLUMN_GUID = "guid";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_DISTANCE = "distance";

        public static Uri buildTrafficUriWithGuid(String guid) {
            return CONTENT_URI.buildUpon()
                    .appendPath(guid)
                    .build();
        }

    }

}
