package com.Utils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.cleanbm.R;

import java.text.DecimalFormat;

/**
 * Created by Ratufa.Paridhi on 8/17/2015.
 */
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 0 meters

    // The minimum time between updates in milliseconds
    private static long MIN_TIME_BW_UPDATES = 10 * 1; // 1 second

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            String strlatitude = "" + new DecimalFormat("###.0####").format(latitude);
                            String strlongitude = "" + new DecimalFormat("###.0####").format(longitude);
                            Utils.setPref(mContext, Constants.USER_LONGITUDE, "" + strlongitude);
                            Utils.setPref(mContext, Constants.USER_LATITUDE, "" + strlatitude);
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                String strlatitude = "" + new DecimalFormat("###.0####").format(latitude);
                                String strlongitude = "" + new DecimalFormat("###.0####").format(longitude);
                                Utils.setPref(mContext, Constants.USER_LONGITUDE, "" + strlongitude);
                                Utils.setPref(mContext, Constants.USER_LATITUDE, "" + strlatitude);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendExceptionReport(e, mContext);
        }

        return location;
    }


    /**
     * Stop using GPS listener Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;

    }

    /**
     * Function to show settings alert dialog On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle(mContext.getString(R.string.GPSsettings));

            // Setting Dialog Message
            alertDialog.setMessage(mContext.getString(R.string.GPSEnable));

            // On pressing Settings button
            alertDialog.setPositiveButton(mContext.getString(R.string.Settings), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);

                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton(mContext.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        String strlatitude = "" + new DecimalFormat("###.0####").format(latitude);
        String strlongitude = "" + new DecimalFormat("###.0####").format(longitude);
        Utils.setPref(mContext, Constants.USER_LONGITUDE, "" + strlongitude);
        Utils.setPref(mContext, Constants.USER_LATITUDE, "" + strlatitude);
        MIN_TIME_BW_UPDATES = 1000 * 5; // 5 second
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
