package com.cleanbm;

import android.app.AlertDialog;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.javabeans.Popup_Menu_Item;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;

/**
 * Created by Ratufa.Paridhi on 8/19/2015.
 */
public class AddNewLocationActivity extends FragmentActivity { //implements LocationListener
    TextView add_this_location, txt_Titlebar;
    PopupWindow popupWindow;
    double lat, longg;
    String full_address = "";
    private GoogleMap mMap;
    private ImageView img_Menu, img_Cancel, img_navigation_icon;
    PopupMenuAdapter adapter;
    private AlertDialogManager alert = new AlertDialogManager();
    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == img_navigation_icon) {
                /*img_Menu.setVisibility(View.GONE);
                img_Cancel.setVisibility(View.VISIBLE);
                // show the list view as dropdown
                popupWindow.showAsDropDown(v, -5, 0);*/
                // Intent in = new Intent(getApplicationContext(),DashBoardActivity.class);
                //  startActivity(in);
                onBackPressed();

            }
          /*  else if(v == img_Cancel)
            {
                //  Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_SHORT).show();
                img_Menu.setVisibility(View.VISIBLE);
                img_Cancel.setVisibility(View.GONE);
            }*/
            else if (v == add_this_location) {

                // Get the middle lat and long on the map view
                VisibleRegion visibleRegion = mMap.getProjection()
                        .getVisibleRegion();
                LatLngBounds bounds1 = visibleRegion.latLngBounds;
                LatLng latLng = bounds1.getCenter();

                Log.e("Tag", latLng.latitude + " " + latLng.longitude);
                lat = latLng.latitude;
                lat=Double.valueOf(String.format("%.6f", lat));
                longg = latLng.longitude;
                longg =  Double.valueOf(String.format("%.6f", longg));

                // Get current user
                ParseUser currentUser = ParseUser.getCurrentUser();

                if (currentUser == null) {
                    // It's an anonymous user, hence show the login screen
                    alert.showAlertDialog(AddNewLocationActivity.this, getResources().getString(R.string.login_first_message));
                } else {
                    // The user is logged in, yay!!
                    Log.i("AddNewLocationActivity", currentUser.getUsername() + " " + currentUser.getObjectId());
                    if (TextUtils.isNullOrEmpty(full_address)) {
                        Toast.makeText(getApplicationContext(), "You cannot add this location.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), AddLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putDouble("Latitude", lat);
                        bundle.putDouble("Longitude", longg);
                        bundle.putString("Address", full_address);
                        intent.putExtras(bundle);
                        //startActivity(intent);
                        Log.d("Add location lat and longg"," "+lat+" "+longg);
                        startActivityForResult(intent,111, bundle);
                    }
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

        // initialize pop up window
        popupWindow = showMenu();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });
        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
        img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
        img_navigation_icon.setOnClickListener(mMenuButtonClickListener);
        txt_Titlebar = (TextView) findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("Add New Location");
        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.GONE);
        add_this_location = (TextView) findViewById(R.id.txtAddLocation);
        add_this_location.setOnClickListener(mMenuButtonClickListener);

        img_Menu =(ImageView)findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);

/*        img_Cancel = (ImageView)findViewById(R.id.cancel_icon);
        // img_Cancel.setVisibility(View.GONE);
        img_Cancel.setOnClickListener(mMenuButtonClickListener);*/

        setUpMapIfNeeded();

    }

    View.OnClickListener mNavigationClickListener = new View.OnClickListener() {
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

    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LayoutInflater inflater = (LayoutInflater) AddNewLocationActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] = new Popup_Menu_Item[5];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        if (currentUser.getUsername() != null && email_verify==true){
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
        adapter = new PopupMenuAdapter(AddNewLocationActivity.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(AddNewLocationActivity.this);
        //ListView lstMenu= (ListView)findViewById(R.id.listView1);
        // lstMenu.setVisibility(View.VISIBLE);

        // set our adapter and pass our pop up window content
        lstMenu.setAdapter(adapter);
        // adapter.notifyDataSetChanged();
        lstMenu.setDivider(getResources().getDrawable(R.drawable.menu_line));
        // lstMenu.setCacheColorHint(Color.TRANSPARENT);
        lstMenu.setAlpha(.93f);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        fadeInAnimation.setDuration(10);
        lstMenu.startAnimation(fadeInAnimation);

        // set the item click listener
        lstMenu.setOnItemClickListener(new DropdownOnItemClickListener());

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        // popupWindow.setWidth(width);
        //  popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        popupWindow.setContentView(lstMenu);
        return popupWindow;
    }


    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {

        String TAG = "AddNewLocationActivity.java";

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

            Popup_Menu_Item popup_menu_item = new Popup_Menu_Item();
            // String data=popup_menu_item.title;
            // String data=parent.getItemAtPosition(position).toString();

            Popup_Menu_Item info = (Popup_Menu_Item) parent.getItemAtPosition(position);
            String data = info.title;

            Log.e("Tag", data);
            if (data.equals(getString(R.string.Home))) {
                finish();
            } else if (data.equals(getString(R.string.search_near_me))) {
                if (Utils.isInternetConnected(AddNewLocationActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alert.showAlertDialog(AddNewLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }

            } else if (data.equals(getString(R.string.search_location))) {
                Intent intent = new Intent(getApplicationContext(), AddNewLocationActivity.class);
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            String latitude = "", longitude = "";
            if (Utils.isInternetConnected(AddNewLocationActivity.this)) {
                latitude = Utils.getPref(getApplicationContext(), Constants.USER_LATITUDE, "0.0");
                longitude = Utils.getPref(getApplicationContext(), Constants.USER_LONGITUDE, "0.0");
                double Gps_lat = ((double) Double.parseDouble(latitude));
                double Gps_lon = ((double) Double.parseDouble(longitude));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Gps_lat, Gps_lon)).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);

                /*mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        VisibleRegion visibleRegion = mMap.getProjection()
                                .getVisibleRegion();
                        LatLngBounds bounds1 = visibleRegion.latLngBounds;
                        LatLng latLng = bounds1.getCenter();
                        Log.e("Tag", latLng.latitude + " " + latLng.longitude);
                    }
                });*/

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (data != null) {
              /*  String myValue = data.getStringExtra("Latitude_added");
                String myValue1 = data.getStringExtra("Longitude_added");
                Log.d("Tag", " my value " + myValue + myValue1);
                Intent in = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("Latitude_added", myValue);
                bundle.putString("Longitude_added", myValue1);
                in.putExtras(bundle);
                setResult(777, in);*/
                finish();
            }
        }

    }

    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {

        // Finding address using reverse Geo Coding
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
