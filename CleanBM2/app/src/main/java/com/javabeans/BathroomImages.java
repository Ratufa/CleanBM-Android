package com.javabeans;

import android.net.Uri;

/**
 * Created by Ratufa.Paridhi on 9/25/2015.
 */
public class BathroomImages {

    public String getBathroom_img_id() {
        return bathroom_img_id;
    }

    public void setBathroom_img_id(String bathroom_img_id) {
        this.bathroom_img_id = bathroom_img_id;
    }

    String bathroom_img_id;

    public String getUser_id_posted_img() {
        return user_id_posted_img;
    }

    public void setUser_id_posted_img(String user_id_posted_img) {
        this.user_id_posted_img = user_id_posted_img;
    }

    String user_id_posted_img;

    public BathroomImages(Uri uri, String user_id_posted_img, String bathroom_img_id) {
        this.uri = uri;
        this.user_id_posted_img = user_id_posted_img;
        this.bathroom_img_id = bathroom_img_id;
    }

    private Uri uri;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
