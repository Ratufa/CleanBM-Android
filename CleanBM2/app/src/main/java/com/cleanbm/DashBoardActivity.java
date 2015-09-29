package com.cleanbm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.Utils.Constants;
import com.Utils.GPSTracker;
import com.Utils.Utils;
import com.adapter.PopupMenuAdapter;
import com.dialog.AlertDialogManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.googleplace.PlaceJSONParser;
import com.javabeans.BathRoomDetail;
import com.javabeans.Popup_Menu_Item;
import com.javabeans.SearchHotel;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.yazeed44.imagepicker.util.Util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Ratufa.Paridhi on 7/27/2015.
 * This class helpful in showing
 * Nearest bathroom, hotel and restaurant on the google map
 */
public class DashBoardActivity extends FragmentActivity {
    double added_latitude;
    double added_longitude;
    // LogCat tag
    private static final String TAG = DashBoardActivity.class.getSimpleName();
    // Google map reference object
    private GoogleMap mMap;
    private ProgressDialog pd;
    ArrayList<BathRoomDetail> array_bathDetails = new ArrayList<BathRoomDetail>();
    ArrayList<SearchHotel> array_Hotel = new ArrayList<SearchHotel>();

    public static int flag_for_login = 0;
    private ImageView img_Menu;
    private AlertDialogManager alert = new AlertDialogManager();
    PopupWindow popupWindow;
    GPSTracker gpsTracker;
    String latitude = "", longitude = "";
    double Gps_lat;
    double Gps_lon;
    LinearLayout StatusBar;
    ProgressBar progressBar;
    PopupMenuAdapter adapter;
    TextView txt_Titlebar;
    ImageView img_navigation_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
       String deviceId= telephonyManager.getDeviceId();

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(deviceId)
                .build();
        mAdView.loadAd(adRequest);

        // initialize pop up window
        popupWindow = showMenu();
        progressBar = (ProgressBar) findViewById(R.id.search_progress);
        StatusBar = (LinearLayout) findViewById(R.id.StatusBar);

        txt_Titlebar = (TextView) findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("CleanBM");

        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
        img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
        img_navigation_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mMenuButtonClickListener);

        Log.e(TAG, "Log2");

        if (Utils.isInternetConnected(DashBoardActivity.this)) {
            try {
                gpsTracker = new GPSTracker(getApplicationContext());
                gpsTracker.getLocation();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        } else {
            alert.showAlertDialog(DashBoardActivity.this, getResources().getString(R.string.connection_not_available));
        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });

        // Current latitude and longitude
        latitude = Utils.getPref(getApplicationContext(), Constants.USER_LATITUDE, "0.0");
        longitude = Utils.getPref(getApplicationContext(), Constants.USER_LONGITUDE, "0.0");
        Gps_lat = ( Double.parseDouble(latitude));
        Gps_lon = ( Double.parseDouble(longitude));

        setUpMapIfNeeded(Gps_lat, Gps_lon, 16);
        if (SearchLocationActivity.flag == 1) {
            // If we click on the Look up this location from Home Screen(SearchLocationactivity)
            // this If block will run.
            Bundle extras = getIntent().getExtras();

            // Get restaurant lat and lng from the Searchlocationactivity
            Double restaurentLat = extras.getDouble("R_LAT");
            Double restaurentLng = extras.getDouble("R_LON");
            Log.d(TAG," Get from SearchLocation "+restaurentLat+" "+restaurentLng);
            setUpMapIfNeeded(restaurentLat, restaurentLng, 12);
            getNearestHotel(restaurentLat, restaurentLng);
            StatusBar.setVisibility(View.GONE);
            SearchLocationActivity.flag = 0;
        } else {
            // If we click on Near me button from Home Screen
            // All nearest bathroom, hotel and restaurant will be show on map
            StatusBar.setVisibility(View.VISIBLE);
            setUpMapIfNeeded(Gps_lat, Gps_lon, 12);
            GetNearestBathRoomUser getNearestBathRoomUser = new GetNearestBathRoomUser();
            getNearestBathRoomUser.execute();
            // This will get Nearest hotel
          //  getNearestHotel(Gps_lat, Gps_lon);

            // Enable current location and also set zoom level
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

        }
    }

    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setActivated(!v.isActivated());
            if (popupWindow.isFocusable()) {
//                isClick = false;
                img_Menu.setImageResource(R.drawable.cancel_icon);
            } else {
//                isClick = true;
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
            popupWindow.showAsDropDown(v, -5, 0);

        }
    };

    /*
        Get all the nearest hotel with lat and longitude
        and show on map
   */
    public void getNearestHotel(Double lat, Double lng) {
       /* if (mMap != null) {
            mMap.clear();
        }*/
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + lat + "," + lng);
        sb.append("&radius=10000");
        sb.append("&types=" + "restaurant");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCJWHBdeonUF9Gafppf6Ag23NRiUhuuzoE"); // AIzaSyCJWHBdeonUF9Gafppf6Ag23NRiUhuuzoE Extra key : AIzaSyCdi7F8PV02m13lhPm3gRQEmsEhWHB_iXk

        // Creating a new non-ui thread task to download json data
        PlacesTask placesTask = new PlacesTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        popupWindow = showMenu();

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });
    }
    private boolean fbUser = false;
    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
        Log.d("Splash screen "," "+email_verify);
        if ((currentUser.getUsername() != null && email_verify==true) || fbUser){
            menus = new Popup_Menu_Item[]{
                    new Popup_Menu_Item(R.drawable.home_icon, getResources().getString(R.string.Home)),
                    new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.my_account)),
                    new Popup_Menu_Item(R.drawable.sign_out_button, getResources().getString(R.string.Logout)),
            };
        } else {
            menus = new Popup_Menu_Item[]{
                    new Popup_Menu_Item(R.drawable.home_icon, getResources().getString(R.string.Home)),
                    new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.Login_menu))
            };
        }

        Log.e("Array size", menus.length + " ");
        adapter = new PopupMenuAdapter(DashBoardActivity.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(DashBoardActivity.this);
        // set our adapter and pass our pop up window content
        lstMenu.setAdapter(adapter);
        lstMenu.setDivider(getResources().getDrawable(R.drawable.menu_line));
        lstMenu.setAlpha(.93f);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        fadeInAnimation.setDuration(10);
        lstMenu.startAnimation(fadeInAnimation);

        // set the item click listener
        lstMenu.setOnItemClickListener(new DropdownOnItemClickListener());

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        // set the list view as pop up window content
        popupWindow.setContentView(lstMenu);
        return popupWindow;
    }

    // On click listener on the Menu option
    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {

        String TAG = "DashBoardActivity.java";

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {

            // get the context and main activity to access variables
            Context mContext = v.getContext();
            DashBoardActivity mainActivity = ((DashBoardActivity) mContext);

            // add some animation when a list item was clicked
            Animation fadeInAnimation = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(10);
            v.startAnimation(fadeInAnimation);

            // img_Menu.setImageResource(R.drawable.cancel_icon);
            img_Menu.setImageResource(R.drawable.menu_icon);
            // dismiss the pop up
            mainActivity.popupWindow.dismiss();

            Popup_Menu_Item info = (Popup_Menu_Item) parent.getItemAtPosition(position);
            String data = info.title;

            Log.e("Tag", data);
            if (data.equals(getString(R.string.Home))) {
                finish();
            } else if (data.equals(getString(R.string.search_near_me))) {
                if (Utils.isInternetConnected(DashBoardActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(),DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                  /*  if (mMap != null) {
                        mMap.clear();}
                    GetNearestBathRoomUser getNearestBathRoomUser = new GetNearestBathRoomUser();
                    getNearestBathRoomUser.execute();
                    // This will get Nearest hotel
                    getNearestHotel(Gps_lat, Gps_lon);

                    // Enable current location and also set zoom level
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setZoomControlsEnabled(true);*/
                } else {
                    alert.showAlertDialog(DashBoardActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getString(R.string.search_location))) {
                Intent intent = new Intent(getApplicationContext(), SearchAdvanceActivity.class);
                //  startActivityForResult(intent, 101);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.add_new_location))) {
                Intent intent = new Intent(getApplicationContext(), AddNewLocationActivity.class);
                //   startActivity(intent);
                startActivityForResult(intent, 777);
                finish();
            } else if (data.equals(getString(R.string.support))) {
                Intent intent = new Intent(getApplicationContext(), SupportActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.my_account))) {
                Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.Logout))) {
                if (Utils.isInternetConnected(DashBoardActivity.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashBoardActivity.this);
                    // Setting Dialog Message
                    alertDialog.setMessage("Do you want to Logout?");
                    // Setting Positive "Yes" Button
                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ParseUser currentUser = ParseUser.getCurrentUser();
                                    if (currentUser.getUsername() != null) {
                                        ParseUser.logOut();
                                        popupWindow = showMenu();
                                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                            @Override
                                            public void onDismiss() {
                                                img_Menu.setImageResource(R.drawable.menu_icon);

                                            }
                                        });
                                    }
                                }
                            });
                    // Setting Negative "NO" Button
                    alertDialog.setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write your code here to invoke NO event
                                    dialog.cancel();
                                }
                            });
                    // Showing Alert Message
                    alertDialog.show();

                } else {
                    alert.showAlertDialog(DashBoardActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getResources().getString(R.string.Login_menu))) {
                flag_for_login = 1;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(in);
                finish();
            }
        }

    }


    public void setUpMarker(double lat, double longg, int i) {

        Log.e("Current lat and long", " " + Gps_lat + " " + Gps_lon);

        if (array_bathDetails.size() != 0) {
            final MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, longg)).title(array_bathDetails.get(i).getBath_full_address());
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.detail_bathroom_icon));
            mMap.addMarker(marker);
           // mMap.setOnInfoWindowClickListener(MarkerrrClickListener);
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    final BathRoomDetail details = GetDisplayUser(marker.getTitle());
                    Log.d(TAG, " marker " + marker.getTitle());
                    final String address = details.getBath_full_address();
                    Log.d(TAG, " " + address);
                    final Dialog dialog = new Dialog(DashBoardActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.map_info_window);
                    ImageView img_report = (ImageView) dialog.findViewById(R.id.img_inappropriate_bathroom);
                    TextView txtMapTitle = (TextView) dialog.findViewById(R.id.txtMapTitle);
                    View view = dialog.findViewById(R.id.viewLine);
                    if (array_bathDetails.contains(details)) {
                        txtMapTitle.setText(address);
                        txtMapTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), DetailBathRoomActivity.class);
                                intent.putExtra("DATA", details);
                                startActivity(intent);
                            }
                        });
                        img_report.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                        DashBoardActivity.this);
                                alertDialog.setMessage("Report as inappropriate?");
                                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                alertDialog.setNegativeButton("NO",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Write your code here to invoke NO event
                                                dialog.cancel();
                                            }
                                        });
                                // Showing Alert Message
                                alertDialog.show();
                            }
                        });

                    }
                    else
                    {
                        img_report.setVisibility(View.GONE);
                        view.setVisibility(View.GONE);
                        final SearchHotel hotel_details = GetHotelDetail(marker.getTitle());
                        final String address_hotel =hotel_details.getAddress();
                        Log.d(TAG," hotel address"+address_hotel);
                        txtMapTitle.setText(address_hotel);
                        txtMapTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                double lat = hotel_details.getLat();
                                double lng = hotel_details.getLongg();
                                Log.d("Lat lng"," "+lat+" "+lng);
                                Intent intent = new Intent(getApplicationContext(), AddLocation.class);
                                Bundle bundle = new Bundle();
                                bundle.putDouble("Latitude", lat);
                                bundle.putDouble("Longitude",lng);
                                bundle.putString("Address", address);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                    }
                    dialog.show();
                    return true;
                }
            });
           // mMap.setInfoWindowAdapter(infoWindowAdapter);
        }
    }

    // http://wptrafficanalyzer.in/blog/customizing-infowindow-contents-in-google-map-android-api-v2-using-infowindowadapter/
    GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
           final BathRoomDetail details = GetDisplayUser(marker.getTitle());
            Log.d(TAG, " marker " + marker.getTitle());
            final String address = details.getBath_full_address();
            Log.d(TAG, " " + address);
            View v = getLayoutInflater().inflate(R.layout.map_info_window, null);
            ImageView img_report = (ImageView) v.findViewById(R.id.img_inappropriate_bathroom);
            TextView txtMapTitle = (TextView) v.findViewById(R.id.txtMapTitle);
            View view = v.findViewById(R.id.viewLine);
            // Getting view from the layout file info_window_layout+
            if (array_bathDetails.contains(details)) {
                txtMapTitle.setText(address);
                txtMapTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), DetailBathRoomActivity.class);
                        intent.putExtra("DATA", details);
                        startActivity(intent);
                    }
                });
                img_report.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                DashBoardActivity.this);
                        alertDialog.setMessage("Report as inappropriate?");
                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.setNegativeButton("NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Write your code here to invoke NO event
                                        dialog.cancel();
                                    }
                                });
                        // Showing Alert Message
                        alertDialog.show();
                    }
                });

        }
            else
            {
                img_report.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
                final SearchHotel hotel_details = GetHotelDetail(marker.getTitle());
                final String address_hotel =hotel_details.getAddress();
                Log.d(TAG," hotel address"+address_hotel);
                txtMapTitle.setText(address_hotel);
                txtMapTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        double lat = hotel_details.getLat();
                        double lng = hotel_details.getLongg();
                        Log.d("Lat lng"," "+lat+" "+lng);
                        Intent intent = new Intent(getApplicationContext(), AddLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putDouble("Latitude", lat);
                        bundle.putDouble("Longitude",lng);
                        bundle.putString("Address", address);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
            return v;
        }
    };

    private GoogleMap.OnInfoWindowClickListener MarkerrrClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            final BathRoomDetail details = GetDisplayUser(marker.getTitle());
            Log.d(TAG, " marker " + marker.getTitle());
            final String address = details.getBath_full_address();
            Log.d(TAG, " " + address);
            final Dialog dialog = new Dialog(DashBoardActivity.this);
            dialog.setContentView(R.layout.map_info_window);
            ImageView img_report = (ImageView) dialog.findViewById(R.id.img_inappropriate_bathroom);
            TextView txtMapTitle = (TextView) dialog.findViewById(R.id.txtMapTitle);
            View view = dialog.findViewById(R.id.viewLine);
            if (array_bathDetails.contains(details)) {
                txtMapTitle.setText(address);
                txtMapTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), DetailBathRoomActivity.class);
                        intent.putExtra("DATA", details);
                        startActivity(intent);
                    }
                });
                img_report.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                DashBoardActivity.this);
                        alertDialog.setMessage("Report as inappropriate?");
                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.setNegativeButton("NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Write your code here to invoke NO event
                                        dialog.cancel();
                                    }
                                });
                        // Showing Alert Message
                        alertDialog.show();
                    }
                });

            }
            else
            {
                img_report.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
                final SearchHotel hotel_details = GetHotelDetail(marker.getTitle());
                final String address_hotel =hotel_details.getAddress();
                Log.d(TAG," hotel address"+address_hotel);
                txtMapTitle.setText(address_hotel);
                txtMapTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        double lat = hotel_details.getLat();
                        double lng = hotel_details.getLongg();
                        Log.d("Lat lng"," "+lat+" "+lng);
                        Intent intent = new Intent(getApplicationContext(), AddLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putDouble("Latitude", lat);
                        bundle.putDouble("Longitude",lng);
                        bundle.putString("Address", address);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
            dialog.show();
          /*  BathRoomDetail details = GetDisplayUser(marker.getTitle());
            String address = details.getBath_full_address();
            String tag= details.getTag();
            Log.e(TAG," "+tag);
            if(array_bathDetails.contains(details)) {

                Intent intent = new Intent(getApplicationContext(), DetailBathRoomActivity.class);
                intent.putExtra("DATA", details);
                startActivity(intent);
            }
            else
            {
                SearchHotel hotel_details = GetHotelDetail(marker.getTitle());
                double lat = hotel_details.getLat();
                double lng = hotel_details.getLongg();
                Log.d("Lat lng"," "+lat+" "+lng);
                Intent intent = new Intent(getApplicationContext(), AddLocation.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("Latitude", lat);
                bundle.putDouble("Longitude",lng);
                bundle.putString("Address", marker.getTitle());
                intent.putExtras(bundle);
                startActivity(intent);
            }*/
        }

    };

    private SearchHotel GetHotelDetail(String title) {
        SearchHotel searchHotel = new SearchHotel();
        for (SearchHotel wp : array_Hotel) {
            if (wp.getTitle().contains(title)) {
                searchHotel = wp;
            }
        }
        return searchHotel;
    }

    private BathRoomDetail GetDisplayUser(String title) {
        BathRoomDetail details = new BathRoomDetail();
        for (BathRoomDetail bathRoomDetail : array_bathDetails) {
            if (bathRoomDetail.getBath_full_address().contains(title)) {
                details = bathRoomDetail;
            }
        }
        return details;
    }

    private BathRoomDetail GetDisplay(String title) {
        Log.d(TAG, "list tilte " + title);
        BathRoomDetail details = new BathRoomDetail();
        for (BathRoomDetail bathRoomDetail : array_bathDetails) {
            if (bathRoomDetail.getBath_full_address().contains(title)) {
                details = bathRoomDetail;
            }
        }
        return details;
    }

    private void setUpMapIfNeeded(final double latitude, final double longitude, int zoomLevel) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(zoomLevel).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    class GetNearestBathRoomUser extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //   StatusBar.setVisibility(View.VISIBLE);
            //   progressBar.setVisibility(View.VISIBLE);
         //   Utils.setProgress(DashBoardActivity.this, true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String latitude = "", longitude = "";
            latitude = Utils.getPref(getApplicationContext(), Constants.USER_LATITUDE, "0.0");
            longitude = Utils.getPref(getApplicationContext(), Constants.USER_LONGITUDE, "0.0");
            final double Gps_lat = (Double.parseDouble(latitude));
            final double Gps_lon = ( Double.parseDouble(longitude));
            // Get the Lat and long
            ParseQuery<ParseObject> query = ParseQuery.getQuery("BathRoomDetail");
          //  query.setLimit(1000);
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(Gps_lat, Gps_lon);
            query.whereWithinKilometers("bathLocation", parseGeoPoint, 10);
            query.whereMatches("approve", "YES");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null) {
                        ParseGeoPoint userLocation;
                        array_bathDetails.clear();
                        for (int i = 0; i < list.size(); i++) {
                            //adresGet = adresGet + MyObject.get(i).getString("Adres") +"\n";

                            userLocation = list.get(i).getParseGeoPoint("bathLocation");
                            double geo_lat = userLocation.getLatitude(); //(double) (userLocation.getLatitude()*1E6);
                            double geo_long = userLocation.getLongitude(); //(double) (userLocation.getLongitude()*1E6);
                            //point1 = new GeoPoint(geo1Int, geo2Int);
                            // String bath_name = list.get(i).getString("bathLocationName");
                            String bath_full_address = list.get(i).getString("bathFullAddress");
                            String bath_room_description = list.get(i).getString("description");
                            double bath_rating = list.get(i).getDouble("bathRating");
                            String bath_id = list.get(i).getObjectId();

                            Log.e(TAG, "Data" + " " + bath_full_address + " " + bath_rating);
                            Log.e(TAG, "Lat n long" + geo_lat + " " + geo_long);

                            BathRoomDetail bathRoomDetail = new BathRoomDetail(bath_id, bath_rating, bath_full_address, geo_lat, geo_long, "BathRoom",bath_room_description);
                            array_bathDetails.add(bathRoomDetail);

                            setUpMarker(geo_lat, geo_long, i);

                            //  setCurrentMarker();
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Gps_lat, Gps_lon)).zoom(18).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            mMap.setMyLocationEnabled(true);
                            UiSettings uiSettings = mMap.getUiSettings();
                            uiSettings.setMyLocationButtonEnabled(true);
                        }

                        Log.e(TAG, "Log 2 "+array_bathDetails.size() );

                        // Get all nearest hotel uncomment it.
                        if(array_bathDetails.size()!=0) {
                            getNearestHotel(Gps_lat, Gps_lon);
                        }
                        progressBar.setVisibility(View.GONE);
                        StatusBar.setVisibility(View.GONE);
                        Log.e(TAG, "Log 1" + array_bathDetails.size());

                    } else {
                        Log.e(TAG, "Error" + e.getLocalizedMessage());
                    }
                }
            });
            return null;
        }

 /*   @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        Log.e(TAG, "Log 2 "+array_bathDetails.size() );

        if(array_bathDetails.size()!=0) {
            getNearestHotel(Gps_lat, Gps_lon);
        }


          *//*  // Get the middle lat and long on the map view
            VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
            LatLngBounds bounds1 = visibleRegion.latLngBounds;
            LatLng latLng = bounds1.getCenter();

            Log.e("Tag", latLng.latitude + " " + latLng.longitude);

            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location=" + latLng.latitude + "," + latLng.longitude);
            sb.append("&radius=10000");
            sb.append("&types=" + "restaurant");
            sb.append("&sensor=true");
            sb.append("&key=AIzaSyCuTCpdsXmh8pmVjXfis0Ta-dBBwHnwPIw"); // AIzaSyCJWHBdeonUF9Gafppf6Ag23NRiUhuuzoE

            // Creating a new non-ui thread task to download json data
            PlacesTask placesTask = new PlacesTask();

            // Invokes the "doInBackground()" method of the class PlaceTask
            placesTask.execute(sb.toString());
           // Utils.setProgress(DashBoardActivity.this, false);*//*

        }*/
    }

    int RESTAURENT = 0;
    int RESTAUREN = 0;

    ArrayList<SearchHotel> mylist;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "" + requestCode + " " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == 777) {
                if (data != null) {
                    String myValue = data.getStringExtra("Latitude_added");
                    String myValue1 = data.getStringExtra("Longitude_added");
                    added_latitude = Double.parseDouble(myValue);
                    added_longitude = Double.parseDouble(myValue1);
                    Log.d(TAG, "Intent Come from add location" + myValue + myValue1);

                    if (mMap != null) {
                        mMap.clear();
                    }

                    // Get the Lat and long
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("BathRoomDetail");
                    query.setLimit(1000);
                    //ParseGeoPoint parseGeoPoint = new ParseGeoPoint(added_latitude, added_longitude);
                    //  query.whereWithinKilometers("bathLocation",parseGeoPoint,10);
                    query.findInBackground(new FindCallback<ParseObject>()

                    {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            if (e == null) {
                                ParseGeoPoint userLocation;
                               // array_bathDetails.clear();
                                for (int i = 0; i < list.size(); i++) {
                                    //adresGet = adresGet + MyObject.get(i).getString("Adres") +"\n";

                                    userLocation = list.get(i).getParseGeoPoint("bathLocation");
                                    double geo_lat = userLocation.getLatitude(); //(double) (userLocation.getLatitude()*1E6);
                                    double geo_long = userLocation.getLongitude(); //(double) (userLocation.getLongitude()*1E6);
                                    //point1 = new GeoPoint(geo1Int, geo2Int);
                                    // String bath_name = list.get(i).getString("bathLocationName");

                                    String bath_full_address = list.get(i).getString("bathFullAddress");
                                    String bath_room_description = list.get(i).getString("description");
                                    double bath_rating = list.get(i).getDouble("bathRating");
                                    String bath_id = list.get(i).getObjectId();

                                    Log.e(TAG, "Data" + " " + bath_full_address + " " + bath_rating);
                                    Log.e(TAG, "Lat n long" + geo_lat + " " + geo_long + bath_id);


                                    BathRoomDetail bathRoomDetail = new BathRoomDetail(bath_id, bath_rating, bath_full_address, geo_lat, geo_long,"BathRoom",bath_room_description);
                                    array_bathDetails.add(bathRoomDetail);

                                    //    setUpAllMarker(geo_lat, geo_long, i);

                                    MarkerOptions marker;

                                    if (geo_lat == added_latitude && geo_long == added_longitude) {

                                        Log.d(TAG + "Set_allmarker", added_latitude + " " + added_longitude + " " + array_bathDetails.get(i).getBath_full_address());
                                        marker = new MarkerOptions().position(new LatLng(geo_lat, geo_long)).title(bath_full_address);
                                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_icon));
                                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(geo_lat, geo_long)).zoom(12).build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                        mMap.setMyLocationEnabled(true);
                                        mMap.getUiSettings().setZoomControlsEnabled(true);
                                        mMap.addMarker(marker);
                                    } else {

                                        marker = new MarkerOptions().position(new LatLng(geo_lat, geo_long)).title(bath_full_address);
                                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.detail_bathroom_icon));
                                        mMap.addMarker(marker);
                                    }



                        /*    MarkerOptions current_marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Added Location");
                            current_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_icon));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Gps_lat, Gps_lon)).zoom(18).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.addMarker(current_marker);*/
                                    //   progressBar.setVisibility(View.GONE);

                                }

                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) {
                                        Log.d(TAG + "marker title", marker.getTitle());
                                        BathRoomDetail details = GetDisplay(marker.getTitle());
                                        Log.d(TAG, " on click " + details.getBath_id());
                                        Intent intent = new Intent(DashBoardActivity.this, DetailBathRoomActivity.class);
                                        intent.putExtra("DATA", details);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    });
                }
            }

        }
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    /**
     * A class, to download Google Places
     */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }
int out=0;
    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Utils.setProgress(DashBoardActivity.this,true);
        }
        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            // Clears all the existing markers
            // mMap.clear();
          //  array_Hotel.clear();
//            Utils.setProgress(DashBoardActivity.this,false);

            for (int i = 0; i < list.size(); i++) {

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");
                Log.d(TAG," google api "+lat+" "+lng);

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                SearchHotel hotel = new SearchHotel(lat,lng,name + " : " + vicinity);
                array_Hotel.add(hotel);
                Log.d(TAG, " gotcha "+array_bathDetails.size());
                for(int j=0;j<array_bathDetails.size();j++) {
                    BathRoomDetail bathRoomDetail = array_bathDetails.get(j);
                    double bath_lat = bathRoomDetail.getLat();
                    double bath_lng = bathRoomDetail.getLongg();
                    if(bath_lat==lat && bath_lng==lng) {
                        Log.d(TAG,"Match true  "+(bath_lat==lat && bath_lng==lng) );
                            out=1;
                        break;
                    }
                }
                if(out==1)
                {
                    Log.d(TAG,"Match true out "+out);
                }
                else {
                    Log.d(TAG, "Add marker" + name + " : " + vicinity);
                    // Creating a marker
                    MarkerOptions markerOptions = new MarkerOptions();

                    // Setting the position for the marker
                    markerOptions.position(latLng);

                    // Setting the title for the marker.
                    //This will be displayed on taping the marker
                    markerOptions.title(name + " : " + vicinity);

                    // set marker icon
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_hotel_location));

                    // Placing a marker on the touched position
                    mMap.addMarker(markerOptions);
                    Log.d(TAG, "Add marker after" + (mMap == null));
                 }
            }
                   // mMap.setOnInfoWindowClickListener(MarkerrrClickListener);
        }
    }


}

