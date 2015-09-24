package com.javabeans;

import java.io.Serializable;

/**
 * Created by Kailash on 16-Sep-15.
 */
public class PlaceLocation implements Serializable {

    Double latitude, longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
