package uk.co.dmott.trafficwarnukbak.data;

import java.util.Date;

/**
 * Created by david on 21/03/17.
 */

public class TrafficPojoEntry {

    private String category1;
    private String category2;
    private String description;
    private String title;
    private String road;
    private String region;
    private String county;
    private double latitude;
    private double longitude;
    private String overallstart;
    private String overallend;

    private String eventstart;
    private String eventend;

    private String author;
    private String link;

    private Date pubdate;

    private Date storedate;
    private String reference;
    private String guid;

    private double distance;



    public TrafficPojoEntry(String pcat1, String pcat2, String pdescription, String ptitle, String proad, String pregion, String pcounty, Double platitude, Double plongitude, String poverallstart, String poverallend, String peventstart, String peventend, String plink, Date ppublisheddate, String preference, String pguid, String pauthor, Double dist) {

        category1 = pcat1;
        category2 = pcat2;
        description = pdescription;
        title = ptitle;
        road = proad;
        region = pregion;
        county = pcounty;
        latitude = platitude;
        longitude = plongitude;
        overallstart = poverallstart;
        overallend = poverallend;
        eventstart = peventstart;
        eventend = peventend;
        link = plink;
        pubdate = ppublisheddate;
        reference = preference;
        guid = pguid;
        author = pauthor;
        distance = dist;


    }

    public TrafficPojoEntry()
    {
    }

    public Date getStoredate() {
        return storedate;
    }

    public void setStoredate(Date storedate) {
        this.storedate = storedate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getOverallstart() {
        return overallstart;
    }

    public void setOverallstart(String overallstart) {
        this.overallstart = overallstart;
    }

    public String getOverallend() {
        return overallend;
    }

    public void setOverallend(String overallend) {
        this.overallend = overallend;
    }

    public String getEventstart() {
        return eventstart;
    }

    public void setEventstart(String eventstart) {
        this.eventstart = eventstart;
    }

    public String getEventend() {
        return eventend;
    }

    public void setEventend(String eventend) {
        this.eventend = eventend;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPubdate() {
        return pubdate;
    }

    public void setPubdate(Date pubdate) {
        this.pubdate = pubdate;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double dist) {
        this.distance = dist;
    }



}
