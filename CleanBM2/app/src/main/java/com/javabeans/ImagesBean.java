package com.javabeans;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Ratufa.Kailash on 09-Sep-15.
 */
public class ImagesBean implements Serializable {

    private Uri uri;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
