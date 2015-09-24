package com.cleanbm;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

/**
 * Created by Ratufa.Paridhi on 7/27/2015.
 */
public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "n2FEMcVdsaoobBxDypgkxF7uSQ3tYBrtDJ4F15zZ", "iqHFj1h4QAoy9H3N2jgDg7xAVszinTZV433TxkCt");

        ParseFacebookUtils.initialize(this);
        ParseUser.enableRevocableSessionInBackground();

        ParseUser.enableAutomaticUser();

        ParseACL parseACL = new ParseACL();

        // If you would like all objects to be private by default, remove this
        // line.
        parseACL.setPublicReadAccess(true);
        parseACL.setPublicWriteAccess(true);

        ParseACL.setDefaultACL(parseACL, true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
