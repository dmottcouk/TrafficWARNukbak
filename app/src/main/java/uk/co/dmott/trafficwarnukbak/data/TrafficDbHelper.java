package uk.co.dmott.trafficwarnukbak.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by david on 21/03/17.
 */

public class TrafficDbHelper extends SQLiteOpenHelper {

    /**
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "traffic.db";

    private static final int DATABASE_VERSION = 16844059;


    public TrafficDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TRAFFIC_TABLE =


                "CREATE TABLE " + TrafficContract.TrafficEntry.TABLE_NAME + " (" +

                /*
                 * TrafficEntry did not explicitly declare a column called "_ID". However,
                 * TrafficEntry implements the interface, "BaseColumns", which does have a field
                 * named "_ID". We use that here to designate our table's primary key.
                 */
                        TrafficContract.TrafficEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        TrafficContract.TrafficEntry.COLUMN_CATEGORY1      + " TEXT NOT NULL, "                 +

                        TrafficContract.TrafficEntry.COLUMN_CATEGORY2      + " TEXT NOT NULL,"                  +

                        TrafficContract.TrafficEntry.COLUMN_DESCRIPTION    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_TITLE  + " TEXT NOT NULL, "                    +

                        TrafficContract.TrafficEntry.COLUMN_ROAD   + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_REGION   + " TEXT NOT NULL, "                    +

                        TrafficContract.TrafficEntry.COLUMN_COUNTY + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_LATITUDE    + " REAL NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_LONGITUDE    + " REAL NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_OVERALLSTART    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_OVERALLEND    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_EVENTSTART    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_EVENTEND    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_AUTHOR    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_LINK    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_PUBDATE    + " INTEGER NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_STOREDATE    + " INTEGER NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_REFERENCE    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_GUID    + " TEXT NOT NULL, "                    +
                        TrafficContract.TrafficEntry.COLUMN_DISTANCE    + " REAL NOT NULL, "                    +
                /*
                 * To ensure this table can only contain one weather entry per date, we declare
                 * the COLUMN_GUID column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                 * SQLite that if we have a traffic entry for a certain COLUMN_GUIDand we attempt to
                 * insert another traffic entry with that COLUMN_GUID, we replace the old traffic entry.
                 */
                        " UNIQUE (" + TrafficContract.TrafficEntry.COLUMN_GUID + ") ON CONFLICT REPLACE);";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        db.execSQL(SQL_CREATE_TRAFFIC_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TrafficContract.TrafficEntry.TABLE_NAME);
        onCreate(db);
    }
}

