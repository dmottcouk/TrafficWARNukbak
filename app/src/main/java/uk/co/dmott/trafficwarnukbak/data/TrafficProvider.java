package uk.co.dmott.trafficwarnukbak.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by david on 21/03/17.
 */

public class TrafficProvider extends ContentProvider {

    public static final int CODE_TRAFFIC = 100;

    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of TrafficProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TrafficDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TrafficContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change.
         */

        /* This URI is content://com.example.android.roadnews */
        matcher.addURI(authority, TrafficContract.PATH_TRAFFIC, CODE_TRAFFIC);


        return matcher;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {


            /*
             * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
             *
             *      content://uk.co.dmott.android.trafficwarnuk/traffic/
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return all of the weather in our traffic table.
             *
             * In this case, we want to return a cursor that contains every row of traffic data
             * in our traffic table.
             */
            case CODE_TRAFFIC: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        TrafficContract.TrafficEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }




    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_TRAFFIC:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(TrafficContract.TrafficEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } catch(Exception e)
                {
                    e.printStackTrace();

                }


                finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

            case CODE_TRAFFIC:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        TrafficContract.TrafficEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException(
                "We are not implementing insert in TrafficWarnUK. Use bulkInsert instead");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("We are not implementing update in TrafficWarnUK");
    }



    @Override
    public boolean onCreate() {
        mOpenHelper = new TrafficDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        throw new RuntimeException("We are not implementing getType in TrafficWarnUK.");
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }














}
