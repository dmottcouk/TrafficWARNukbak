package uk.co.dmott.trafficwarnukbak.utilities;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.co.dmott.trafficwarnukbak.data.TrafficPojoEntry;

import static uk.co.dmott.trafficwarnukbak.utilities.NetworkUtils.getUnplannedUrl;

/**
 * Created by david on 21/03/17.
 */

public class TrafficXmlUtils {

    private static final String TAG = TrafficXmlUtils.class.getSimpleName();
    private static final String ns = null;

    private static List<TrafficPojoEntry> mtrafficitems;     // this is the array we build from XMLPullparser

    public static List<TrafficPojoEntry> getTrafficDataFromXML()
    {

        try {

            Log.d(TAG, "In XmlPullParser");
            mtrafficitems= new ArrayList<TrafficPojoEntry>();


            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            // Parser supports XML namespaces
            factory.setNamespaceAware(true);

            // Provides the methods needed to parse XML documents
            XmlPullParser parser = factory.newPullParser();

            // InputStreamReader converts bytes of data into a stream
            // of characters
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new InputStreamReader(NetworkUtils.getUnplannedUrlData(getUnplannedUrl())));

            // Passes the parser and the first tag in the XML document
            // for processing

            beginDocument(parser, "rss");   // works
            readFeed(parser);


            Log.d("test", "Size of traffic items " + mtrafficitems.size());
            Log.d("test", "Finished reading feed");

            Thread.sleep(5000);


        } catch (XmlPullParserException e) {

            e.printStackTrace();
        } catch (URISyntaxException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (ParseException e) {

            e.printStackTrace();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }


        return mtrafficitems;
    }


    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;

        // next() advances to the next element in the XML
        // document being a starting or ending tag, or a value
        // or the END_DOCUMENT

        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
        }

        // Throw an error if a start tag isn't found

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        // Verify that the tag passed in is the first tag in the XML
        // document

        String topTag = parser.getName();

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    private static List<TrafficPojoEntry> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {


        //parser.require(XmlPullParser.START_TAG, ns, "lfm");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals("brdgdfgfdg")) {

                tag = parser.getName();

            } else if (tag.equals("channel")) {
                readChannel(parser);
            }
            else {
                skip(parser);
            }
        }
        return mtrafficitems;
    }


    private static void readChannel(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals("item")) {
                mtrafficitems.add(readTrafficItem(parser));
            } else


            {
                skip(parser);
            }

        }
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException,
            IOException {

        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "skip");

        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static TrafficPojoEntry readTrafficItem(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {


        String author = "dummy";
        String [] category = {"dummy","dummy"};
        String description = "dummy";
        String title = "dummy";
        String road = "dummy";
        String region = "dummy";
        String county = "dummy";
        Double latitude = 0.0;
        Double longitude = 0.0;
        String overallstart = "dummy";
        String overallend = "dummy";
        String eventstart = "dummy";
        String eventend = "dummy";
        String link = "dummy";
        Date publisheddate = Calendar.getInstance().getTime();
        String reference = "dummy";
        String guid = "dummy";

        int categoryindex = 0;


        int i = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals("author")) {
                author = readAuthor(parser);
            } else if (tag.equals("category")) {
                category[categoryindex++] = readCategory(parser);

            } else if (tag.equals("description")) {
                description = readDescription(parser);

            } else if (tag.equals("title")) {
                title = readTitle(parser);

            } else if (tag.equals("road")) {
                road = readRoad(parser);

            } else if (tag.equals("region")) {
                region = readRegion(parser);

            } else if (tag.equals("county")) {
                county = readCounty(parser);

            } else if (tag.equals("latitude")) {
                latitude = readLatitude(parser);

            } else if (tag.equals("longitude")) {
                longitude = readLongitude(parser);

            } else if (tag.equals("overallStart")) {
                overallstart = readOverallStart(parser);

            } else if (tag.equals("overallEnd")) {
                overallend = readOverallEnd(parser);

            } else if (tag.equals("eventStart")) {
                eventstart = readEventStart(parser);

            } else if (tag.equals("eventEnd")) {
                eventend = readEventEnd(parser);
            } else if (tag.equals("link")) {
                eventend = readLink(parser);
            } else if (tag.equals("pubDate")) {
                publisheddate = readPubDate(parser);
            } else if (tag.equals("guid")) {
                guid = readGuid(parser);
            } else if (tag.equals("reference")) {
                guid = readReference(parser);

            }

            else {
                skip(parser);
            }
        }



        return new TrafficPojoEntry(category[0], category[1], description, title, road, region, county, latitude, longitude, overallstart, overallend,eventstart,eventend,link,publisheddate,reference,guid,author,-1.0);

    }

    private static String readReference(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(Tag, "readAuthor");
        parser.require(XmlPullParser.START_TAG, ns, "reference");
        String guid = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "reference");
        return guid;
    }



    private static String readGuid(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(Tag, "readAuthor");
        parser.require(XmlPullParser.START_TAG, ns, "guid");
        String guid = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "guid");
        return guid;
    }


    private static String readLink(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(Tag, "readAuthor");
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }



    private static String readAuthor(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(Tag, "readAuthor");
        parser.require(XmlPullParser.START_TAG, ns, "author");
        String category = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "author");
        return category;
    }

    private static Date readPubDate(XmlPullParser parser) throws IOException,
            XmlPullParserException,ParseException {
        //Log.d(Tag, "readPubDate");
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String pubDateString = readText(parser);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        Date pdate = sdf.parse(pubDateString);


        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return pdate;
    }




    private static String readCategory(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "category");
        String category = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "category");
        return category;
    }

    private static String readDescription(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readDescription");
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    private static String readTitle(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readServiceTitle");
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    private static String readRoad(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readDescription");
        parser.require(XmlPullParser.START_TAG, ns, "road");
        String road = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "road");
        return road;
    }

    private static String readRegion(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readDescription");
        parser.require(XmlPullParser.START_TAG, ns, "region");
        String region = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "region");
        return region;
    }



    private static String readCounty(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "county");
        String county = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "county");
        return county;
    }

    private static Double readLatitude(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "latitude");
        String latitude = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "latitude");
        return Double.parseDouble(latitude);
    }

    private static Double readLongitude(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "longitude");
        String longitude = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "longitude");
        return Double.parseDouble(longitude);
    }

    private static String readOverallStart(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "overallStart");
        String start = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "overallStart");
        return start;
    }

    private static String readOverallEnd(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "overallEnd");
        String end = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "overallEnd");
        return end;
    }

    private static String readEventStart(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "eventStart");
        String start = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "eventStart");
        return start;
    }

    private static String readEventEnd(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readCategory");
        parser.require(XmlPullParser.START_TAG, ns, "eventEnd");
        String end = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "eventEnd");
        return end;
    }



    private static String readText(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        //Log.d(MainActivity.MUSIC_SEARCHES_SYMBOL, "readText");
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }






































}
