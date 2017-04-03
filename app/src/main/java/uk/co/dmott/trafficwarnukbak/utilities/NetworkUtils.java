package uk.co.dmott.trafficwarnukbak.utilities;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by david on 21/03/17.
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String trafficURLUnplanned = "http://m.highways.gov.uk/feeds/rss/UnplannedEvents.xml";
    private static final String trafficURLTotal = "http://m.highways.gov.uk/feeds/rss/AllEvents.xml";


    public static URL getUnplannedUrl() {


        try {
            URL trafficQueryUrl = new URL(trafficURLUnplanned );
            Log.v(TAG, "URL: " + trafficQueryUrl);
            return trafficQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static InputStream getUnplannedUrlData(URL url) throws URISyntaxException, IOException {

        URL url2 = url;
        HttpURLConnection urlConnection = (HttpURLConnection) url2.openConnection();
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        return in;


    }










}
