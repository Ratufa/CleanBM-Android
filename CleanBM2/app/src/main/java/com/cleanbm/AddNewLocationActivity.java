package com.cleanbm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.Utils.Constants;
import com.Utils.TextUtils;
import com.Utils.Utils;
import com.adapter.PopupMenuAdapter;
import com.dialog.AlertDialogManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.javabeans.BathRoomDetail;
import com.javabeans.Popup_Menu_Item;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.yazeed44.imagepicker.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ratufa.Paridhi on 8/19/2015.
 *  Setting the location on the google map where you want to add location of bathroom
 *  and click on Add this location Button
 */
public class AddNewLocationActivity extends FragmentActivity {
    TextView add_this_location, txt_Titlebar;
    PopupWindow popupWindow;
    double lat, longg;
    String full_address = "";
    private GoogleMap mMap;
    private ImageView img_Menu, img_navigation_icon,img_add_new_location;
    PopupMenuAdapter adapter;
    private AlertDialogManager alert = new AlertDialogManager();

    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                /*
                    On Back icon click listener
                */
            if (v == img_navigation_icon) {
                onBackPressed();
            }
            /*
                User click on Add this location button
                Setting the location by moving google map and get the lat and long
                and also fetch the Address from the lat and lng
            */
            else if (v == add_this_location) {

                // Get the middle lat and long on the map view
                VisibleRegion visibleRegion = mMap.getProjection()
                        .getVisibleRegion();
                LatLngBounds bounds1 = visibleRegion.latLngBounds;
                LatLng latLng = bounds1.getCenter();

                Log.e("Tag", latLng.latitude + " " + latLng.longitude);
                lat = latLng.latitude;
                // Set the lat and lng with 6 decimal place
                lat=Double.valueOf(String.format("%.6f", lat));
                longg = latLng.longitude;
                longg =  Double.valueOf(String.format("%.6f", longg));

                if (TextUtils.isNullOrEmpty(full_address)) {
                        Toast.makeText(getApplicationContext(), "You cannot add this location.", Toast.LENGTH_SHORT).show();
                    } else {
                    /*
                        WHen user set the marker on the google map, it pass the lat and long
                        and addess with intent.
                    */
                        Intent intent = new Intent(getApplicationContext(), AddLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putDouble("Latitude", lat);
                        bundle.putDouble("Longitude", longg);
                        bundle.putString("Address", full_address);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,111, bundle);
                    }

            }
        }
    };
    private LatLng latLng;
    private String city;
    private String state;
    private String postal_code;
    private String country;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_location);

        img_add_new_location = (ImageView)findViewById(R.id.img_add_new_location);

        // initialize pop up window
        popupWindow = showMenu();
        // On Dismiss pop up, icon get changed.
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });
         /*
            Home icon get changed to Back icon and set the on click listener
            on the back icon.
        */
        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
        img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
        img_navigation_icon.setOnClickListener(mMenuButtonClickListener);
         /*
            Setting the action bar title.
        */
        txt_Titlebar = (TextView) findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("Add New Location");
        /*
            Navigation (menu icon) visiblity on and set the on click listener on menu.
       */
        img_Menu =(ImageView)findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);

        add_this_location = (TextView) findViewById(R.id.txtAddLocation);
        add_this_location.setOnClickListener(mMenuButtonClickListener);

        // Setting up the map
        setUpMapIfNeeded();

    }

    /*
        On menu button click, Popup window will be open up
        and also changes the icon.
    */
    View.OnClickListener mNavigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setActivated(!v.isActivated());
            if (popupWindow.isFocusable()) {
                img_Menu.setImageResource(R.drawable.cancel_icon);
            } else {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
            popupWindow.showAsDropDown(v, -5, 0);
        }
    };

    /*
       Showing the pop up menu.

    */
    Boolean fbUser=false;
    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[];
        // Getting the Emailverified value from the parse to check whether User email is verified or not
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        /*
            If User is verify and successfully login
            then If block will be run and showing "Logout" option.
        */
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
         /*
            If User is not verify
            then else block will be run and showing "Sign Up" option.
         */
            menus = new Popup_Menu_Item[]{
                    new Popup_Menu_Item(R.drawable.home_icon, getResources().getString(R.string.Home)),
                    new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.Login_menu))
            };
        }

        // Setting the popup Menu list in the adapter
        adapter = new PopupMenuAdapter(AddNewLocationActivity.this, R.layout.popup_menu_item, menus);
        //the drop down list in a listview
        ListView lstMenu = new ListView(AddNewLocationActivity.this);
        // set our adapter and pass our pop up window content
        lstMenu.setAdapter(adapter);
        // adapter.notifyDataSetChanged();
        lstMenu.setDivider(getResources().getDrawable(R.drawable.menu_line));
        // Setting the alpha on list
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

    /*
           WHen we click on the Pop up menu list item then, this listener will be called
       */
    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
            // get the context and main activity to access variables
            Context mContext = v.getContext();
            AddNewLocationActivity mainActivity = ((AddNewLocationActivity) mContext);

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

            if (data.equals(getString(R.string.Home))) {
                // Click on Home menu, it finish the current activity
                finish();
            } else if (data.equals(getString(R.string.search_near_me))) {
                // Click on Search Near me menu, It intent to the DashBoardActivity
                // Showing nearest bathroom and hotel/restaurant
                if (Utils.isInternetConnected(AddNewLocationActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If internet is not present.
                    alert.showAlertDialog(AddNewLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }

            } else if (data.equals(getString(R.string.search_location))) {
                // Click on Search Location
                Intent intent = new Intent(getApplicationContext(), SearchAdvanceActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.add_new_location))) {
                // Click on Add new Location menu
                Intent intent = new Intent(getApplicationContext(), AddNewLocationActivity.class);
                startActivityForResult(intent, 777);
                finish();
            } else if (data.equals(getString(R.string.support))) {
                // Support Menu
                Intent intent = new Intent(getApplicationContext(), SupportActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.my_account))) {
                // My Account Menu
                Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
                startActivity(intent);
                finish();
            } else if (data.equals(getString(R.string.Logout))) {
                if (Utils.isInternetConnected(AddNewLocationActivity.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddNewLocationActivity.this);
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
                    alert.showAlertDialog(AddNewLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getResources().getString(R.string.Login_menu))) {
                //   flag_for_login = 1;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                in.putExtra("BathDescription", "");
                startActivity(in);
                finish();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // initialize pop up window
        popupWindow = showMenu();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });
    }

    /*
        set up the Map id
        Get the current lat and lng from the shared preference
        Animate the camera to the current lat lng
        on setting camera get the latlgn and convert the latlng to Address using ReverseGeocodingTask
    */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            String latitude = "", longitude = "";
            if (Utils.isInternetConnected(AddNewLocationActivity.this)) {
                latitude = Utils.getPref(getApplicationContext(), Constants.USER_LATITUDE, "0.0");
                longitude = Utils.getPref(getApplicationContext(), Constants.USER_LONGITUDE, "0.0");
                double Gps_lat = (Double.parseDouble(latitude));
                double Gps_lon = (Double.parseDouble(longitude));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Gps_lat, Gps_lon)).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);

                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        latLng = mMap.getCameraPosition().target;
                        new ReverseGeocodingTask().execute(latLng);
                    }
                });

            } else {
                alert.showAlertDialog(AddNewLocationActivity.this, getResources().getString(R.string.connection_not_available));

            }
        }
    }
    String  TAG ="AddNewLocationActivity";
    ArrayList<BathRoomDetail> array_bathDetails = new ArrayList<BathRoomDetail>();
    ProgressDialog pd = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG," "+requestCode+" "+resultCode);
        if(resultCode==RESULT_OK) {
            if (requestCode == 111) {
                if (data != null) {
                    String message=data.getStringExtra("MESSAGE");
                    if(message.equalsIgnoreCase("Submit")) {
                        add_this_location.setVisibility(View.GONE);
                        img_add_new_location.setVisibility(View.GONE);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat, longg)).zoom(12).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        Log.d("AddNewLocation", "Intent Come from add location" + lat + longg);

                        if (mMap != null) {
                            mMap.clear();
                        }

                        new AsyncTask<Void,Void,Void>()
                        {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                setProgress(true);
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                // Get the Lat and long
                                ParseQuery<ParseObject> query = ParseQuery.getQuery("BathRoomDetail");
                                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lat, longg);
                                query.whereWithinKilometers("bathLocation", parseGeoPoint, 10);
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


                                                BathRoomDetail bathRoomDetail = new BathRoomDetail(bath_id, bath_rating, bath_full_address, geo_lat, geo_long, "BathRoom", bath_room_description);
                                                array_bathDetails.add(bathRoomDetail);

                                                //    setUpAllMarker(geo_lat, geo_long, i);

                                                MarkerOptions marker;

                                                if (geo_lat == lat && geo_long == longg) {

                                                    Log.d(TAG + "Set_allmarker", lat + " " + longg + " " + array_bathDetails.get(i).getBath_full_address());
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

                                            }

                                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                                @Override
                                                public void onInfoWindowClick(Marker marker) {
                                                    Log.d(TAG + "marker title", marker.getTitle());
                                                    BathRoomDetail details = GetDisplayUser(marker.getTitle());
                                                    Log.d(TAG, " on click " + details.getBath_id());
                                                    Intent intent = new Intent(AddNewLocationActivity.this, DetailBathRoomActivity.class);
                                                    intent.putExtra("DATA", details);
                                                    startActivity(intent);

                                                }
                                            });
                                        }
                                    }
                                });
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                               setProgress(false);
                            }
                        }.execute();

                    }

                }
            }
        }

    }

    public void setProgress(boolean visibility) {
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(AddNewLocationActivity.this);
                    pd.setTitle("");
                    pd.setMessage(getString(R.string.loading));
                    pd.setCancelable(false);
                    pd.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_dialog));
                    pd.setCancelable(true);
                    if (!pd.isShowing())
                        pd.show();
                } else {
                    try {
                        if (!pd.isShowing())
                            pd.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        } else {
            try {
                long delayInMillis = 5000;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                }, delayInMillis);
               /* if (pd.isShowing())
                    pd.dismiss();*/
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        }
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
    // Finding address using reverse Geo Coding
    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        @Override
        protected String doInBackground(LatLng... params) {
            StringBuilder result = new StringBuilder();
            Geocoder geocoder = new Geocoder(AddNewLocationActivity.this);
            double latitude = params[0].latitude;
            double longitude = params[0].longitude;

            Log.i("latitude is : ", "" + latitude + "longitude is :" + longitude);

            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    city = address.getLocality();
                    postal_code = address.getPostalCode();
                    country = address.getCountryName();
                    data = address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "";
                    state = address.getAdminArea();

                    if (data != null) {
                        result.append(data + ", ");
                        Log.d("data>>> ", data);
                    }

                    if (city != null) {
                        result.append(city + ", ");
                        Log.d("city>>> ", city);
                    }

                    if (country != null) {
                        result.append(country + ", ");
                        Log.d("Country>>> ", country);
                    }

                    if (state != null) {
                        result.append(state);
                        Log.d("State>>> ", state);
                    }

                    if (postal_code != null) {
                        result.append(postal_code);
                        Log.d("postal_code>>> ", postal_code);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String addressText) {
            Log.i("post Address", addressText);
            //tv_address.setText(addressText);
            full_address = addressText;
        }
    }

}

