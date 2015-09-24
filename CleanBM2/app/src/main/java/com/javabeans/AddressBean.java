package com.javabeans;

import java.io.Serializable;

/**
 * Created by Ratufa.Paridhi on 9/4/2015.
 */
public class AddressBean implements Serializable {

    private String address;
    private String lat, lng;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
