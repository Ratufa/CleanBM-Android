package com.javabeans;

import java.io.Serializable;

/**
 * Created by Ratufa.Paridhi on 8/21/2015.
 */
public class BathRoomDetail implements Serializable{

    String bath_id;
    double bath_rating;
    String bath_full_address;

    public String getBath_room_description() {
        return bath_room_description;
    }

    public void setBath_room_description(String bath_room_description) {
        this.bath_room_description = bath_room_description;
    }

    String bath_room_description;

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    String Tag;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLongg() {
        return longg;
    }

    public void setLongg(Double longg) {
        this.longg = longg;
    }

    Double lat=0.0;
    Double longg=0.0;

    public BathRoomDetail(String bath_id, double bath_rating, String bath_full_address, Double lat, Double longg,String Tag, String bath_room_description) {
        this.bath_id = bath_id;
        this.bath_rating = bath_rating;
        this.bath_full_address = bath_full_address;
        this.bath_room_description=bath_room_description;
        this.lat = lat;
        this.longg = longg;
        this.Tag=Tag;
    }
    public BathRoomDetail(String id, double bath_rating, String bath_full_address) {
        this.bath_id = id;
        //this.bath_location_name=bath_location_name;
        this.bath_full_address = bath_full_address;
        this.bath_rating = bath_rating;
    }

    public BathRoomDetail() {

    }

    public String getBath_full_address() {
        return bath_full_address;
    }

    public void setBath_full_address(String bath_full_address) {
        this.bath_full_address = bath_full_address;
    }

    public String getBath_id() {
        return bath_id;
    }

    public void setBath_id(String bath_id) {
        this.bath_id = bath_id;
    }


    public double getBath_rating() {
        return bath_rating;
    }

    public void setBath_rating(double bath_rating) {
        this.bath_rating = bath_rating;
    }


}
