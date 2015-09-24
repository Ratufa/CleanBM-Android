package com.cleanbm;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Ratufa.Paridhi on 8/28/2015.
 */
public class BaseActivityy extends FragmentActivity implements LocationListener {

    protected LocationManager mLocationManager;
    private static final int GPS_ENABLE_REQUEST = 0x1001;
    private static final int WIFI_ENABLE_REQUEST = 0x1006;
    static int flag=0;

    private BroadcastReceiver mNetworkDetectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //checkInternetConnection();
        }
    };
    private AlertDialog mInternetDialog;
    private AlertDialog mGPSDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        registerReceiver(mNetworkDetectReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        mLocationManager.removeUpdates(this);
        unregisterReceiver(mNetworkDetectReceiver);
        super.onDestroy();
    }

//	public void checkInternetConnection() {
//		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo ni = manager.getActiveNetworkInfo();
//
//		if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
//
//		} else {
//			showNoInternetDialog();
//		}
//	}

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDiabledDialog();
        }
    }

    protected void showNoInternetDialog() {

        if (mInternetDialog != null && mInternetDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internet Disabled!");
        builder.setMessage("No active Internet connection found.");
        builder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//				Intent internetOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
//				startActivityForResult(internetOptionsIntent, WIFI_ENABLE_REQUEST);
            }
        }).setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        mInternetDialog = builder.create();
        mInternetDialog.show();
    }

    public void showGPSDiabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Disabled");
        builder.setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device");
        builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST);
               // finish();
               // System.exit(0);
            }
        }).setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        mGPSDialog = builder.create();
        mGPSDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GPS_ENABLE_REQUEST) {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }

            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDiabledDialog();
            }
        } else if (requestCode == WIFI_ENABLE_REQUEST) {

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
