package com.javabeans;

import java.io.Serializable;

/**
 * Created by Ratufa.Paridhi on 9/4/2015.
 */
public class SearchHotel implements Serializable {

    public SearchHotel() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongg() {
        return longg;
    }

    public void setLongg(double longg) {
        this.longg = longg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    double lat;
    double longg;
    String title;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    String address;

    public SearchHotel(double lat, double longg, String title) {
        this.lat = lat;
        this.longg = longg;
        this.title = title;
    }


}
