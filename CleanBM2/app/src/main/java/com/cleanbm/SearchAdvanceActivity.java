package com.cleanbm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.Utils.Constants;
import com.Utils.GPSTracker;
import com.Utils.Utils;
import com.adapter.AddressAdapter;
import com.adapter.PopupMenuAdapter;
import com.adapter.SpinnerAdapter;
import com.dialog.AlertDialogManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.googleplace.GooglePlaces;
import com.googleplace.PlaceDetailsJSONParser;
import com.googleplace.PlaceJSONParser;
import com.javabeans.AddressBean;
import com.javabeans.PlaceDetails;
import com.javabeans.PlaceLocation;
import com.javabeans.PlacesList;
import com.javabeans.PlacesSuggestionsBean;
import com.javabeans.Popup_Menu_Item;
import com.javabeans.SearchHotel;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
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
 * Created by Ratufa.Paridhi on 9/5/2015.
 */
public class SearchAdvanceActivity extends FragmentActivity {

    // img : http://www.androidbegin.com/tutorial/android-parse-com-simple-listview-tutorial/
    // http://stackoverflow.com/questions/23289380/this-ip-site-or-mobile-application-is-not-authorized-to-use-this-api-key-with-i
    String TAG = "SearchAdvanceActivity";
    ArrayList<String> contactList;
    ArrayList<SearchHotel> array_Hotel = new ArrayList<SearchHotel>();

    AutoCompleteTextView autoCompView;
    //TextView txtLookUpThisLocation;
    // ImageView img_near_me;
    AlertDialogManager alert = new AlertDialogManager();
    public static int flag = 0;
    String data;

    ArrayList<SearchHotel> arrayList_hotel = new ArrayList<SearchHotel>();

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();
    // HashMap<String, String>  placesListItems1 = new HashMap<String, String>();
    // Places List
    PlacesList nearPlaces;
    // Progress dialog
    ProgressDialog pDialog;
    // Google Places
    GooglePlaces googlePlaces;
    String latitude, longitude;
    double Gps_lat, Gps_lon;
    GPSTracker gpsTracker;
    private GoogleMap mMap;

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name
    // String URL = "http://maps.googleapis.com/maps/api/geocode/json?address="+reference+"&sensor=false";
    private ImageView img_Menu, img_Cancel, img_navigation_icon;
    // Place Details
    TextView txt_Titlebar;
    PlaceDetails placeDetails;
    AddressBean address = new AddressBean();

    // for google place
    private DownloadTask placesDownloadTask;
    private DownloadTask placeDetailsDownloadTask;
    private ParserTask placesParserTask;
    private DetailParserTask placeDetailsParserTask;
    final int PLACES = 0;
    PopupMenuAdapter adapter;
    final int PLACES_DETAILS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // AIzaSyArlaP-idISleDG7CS1c3jPoFlBcvL-ZJE
        setContentView(R.layout.activity_advance_search);
        // initialize pop up window
        popupWindow = showMenu();

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
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
        txt_Titlebar.setText("Search Location");
        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);

        if (Utils.isInternetConnected(SearchAdvanceActivity.this)) {
            try {
                gpsTracker = new GPSTracker(getApplicationContext());
                gpsTracker.getLocation();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        } else {
            alert.showAlertDialog(SearchAdvanceActivity.this, getResources().getString(R.string.connection_not_available));
        }
        contactList = new ArrayList<String>();
        latitude = Utils.getPref(getApplicationContext(), Constants.USER_LATITUDE, "0.0");
        longitude = Utils.getPref(getApplicationContext(), Constants.USER_LONGITUDE, "0.0");
        Gps_lat = ((double) Double.parseDouble(latitude));
        Gps_lon = ((double) Double.parseDouble(longitude));
        setUpMapIfNeeded(Gps_lat, Gps_lon, 16);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.edtSearchLocation);
        Utils.hideKeyBoard(getApplicationContext(), autoCompView);

        autoCompView.addTextChangedListener(new TextWatcher() {
            private boolean shouldAutoComplete = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //    handleIntent(getIntent());
                shouldAutoComplete = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (shouldAutoComplete) {
//                    data = s.toString();
//                    if (Utils.isInternetConnected(SearchAdvanceActivity.this)) {
//                        new GetAddressTask().execute(s.toString().trim().replaceAll("\\s+", ""));
//                    } else {
//                        alert.showAlertDialog(SearchAdvanceActivity.this, getResources().getString(R.string.connection_not_available));
//                    }
//                }

                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GooglePlaces.getAutoCompleteUrl(SearchAdvanceActivity.this, s.toString());
                Log.d("URL : ", url);

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);

            }
        });


        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Utils.hideKeyBoard(SearchAdvanceActivity.this, autoCompView);

                autoCompView.setText(suggestionsList.get(position).getDescription());

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

                // Getting url to the Google Places details api
                String url = GooglePlaces.getPlaceDetailsUrl(SearchAdvanceActivity.this, suggestionsList.get(position).getReference());

                Log.d(TAG, "Detail URL :" + url);

                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);


            }
        });
    }

    PopupWindow popupWindow;
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
        LayoutInflater inflater = (LayoutInflater) SearchAdvanceActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] ;//= new Popup_Menu_Item[5];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        if (currentUser.getUsername() != null && email_verify==true) {
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
        adapter = new PopupMenuAdapter(SearchAdvanceActivity.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(SearchAdvanceActivity.this);
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

        String TAG = "SearchAdvanceActivity.java";

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {

            // get the context and main activity to access variables
            Context mContext = v.getContext();
            SearchAdvanceActivity mainActivity = ((SearchAdvanceActivity) mContext);

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
                if (Utils.isInternetConnected(SearchAdvanceActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alert.showAlertDialog(SearchAdvanceActivity.this, getResources().getString(R.string.connection_not_available));
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
                if (Utils.isInternetConnected(SearchAdvanceActivity.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchAdvanceActivity.this);
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
                    alert.showAlertDialog(SearchAdvanceActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getResources().getString(R.string.Login_menu))) {
                //   flag_for_login = 1;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(in);
                finish();
            }
        }

    }

    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == img_navigation_icon) {
                onBackPressed();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    private void setUpMapIfNeeded(double lat, double longg, int zoomLevel) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat, longg)).zoom(zoomLevel).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }


    private ProgressDialog pd;

    public void setProgress(boolean visibility) {
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(SearchAdvanceActivity.this);
                    pd.setTitle("");
                    pd.setCancelable(false);
                    pd.setMessage(getString(R.string.loading));
                    pd.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_dialog));
                    pd.setCancelable(true);
                    if (!pd.isShowing())
                        pd.show();
                } else {
                    if (!pd.isShowing())
                        pd.show();
                }
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        } else {
            try {
               /* long delayInMillis = 5000;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                }, delayInMillis);*/
                if (pd.isShowing())
                    pd.dismiss();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        }
    }

    ArrayList<AddressBean> placesList = new ArrayList<AddressBean>();

    private class GetAddressTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
//            ArrayList<AddressBean> list = null;

            JSONObject jsonObject = null;

            Log.d("URL Data Task", params[0]);
            try {
                URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + params[0] + "&sensor=false");

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
                Log.d("data fech", data);
                br.close();

                try {
                    jsonObject = new JSONObject(data);
                    return jsonObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.d("Exception ", e.toString());
            } finally {
                try {
                    iStream.close();
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);


            if (jsonObject != null) {

                try {
                    JSONArray jPlaces = jsonObject.getJSONArray("results");
                    placesList.clear();

                    for (int i = 0; i < jPlaces.length(); i++) {
                        try {
                            AddressBean place = new AddressBean();
                            JSONObject jPlace = jPlaces.getJSONObject(i);

                            place.setAddress(jPlace.getString("formatted_address"));
                            place.setLat(jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                            place.setLng(jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                            placesList.add(place);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), list, R.layout.spinner_item, from, to);
//                ArrayAdapter adapter = new ArrayAdapter(SearchLocationActivity.this, android.R.layout.simple_list_item_1, list);
                AddressAdapter adapter = new AddressAdapter(SearchAdvanceActivity.this, placesList);

                // Setting the adapter
                autoCompView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } else {

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
//            Log.d("downloadUrl : ", data);

            br.close();

        } catch (Exception e) {
            Log.d("Exception download url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        private int downloadType = 0;

        // Constructor
        public DownloadTask(int type) {
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("DownloadTask : ", result);
            switch (downloadType) {
                case PLACES:
                    // Creating ParserTask for parsing Google Places
                    placesParserTask = new ParserTask();

                    // Start parsing google places json data
                    // This causes to execute doInBackground() of ParserTask class
                    placesParserTask.execute(result);

                    break;

                case PLACES_DETAILS:
                    // Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new DetailParserTask();

                    // Starting Parsing the JSON string
                    // This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    ArrayList<PlacesSuggestionsBean> suggestionsList = null;

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, ArrayList<PlacesSuggestionsBean>> {

        @Override
        protected ArrayList<PlacesSuggestionsBean> doInBackground(String... jsonData) {

            JSONObject jObject;

            try {
                jObject = new JSONObject(jsonData[0]);

                PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                // Getting the parsed data as a List construct
                suggestionsList = placeJsonParser.getSuggestionsList(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return suggestionsList;
        }

        @Override
        protected void onPostExecute(ArrayList<PlacesSuggestionsBean> result) {
            // Creating a SimpleAdapter for the AutoCompleteTextView
            SpinnerAdapter adapter = new SpinnerAdapter(SearchAdvanceActivity.this, result);
            // Setting the adapter
            autoCompView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    ArrayList<PlaceLocation> locationList = null;

    private class DetailParserTask extends AsyncTask<String, Integer, ArrayList<PlaceLocation>> {

        @Override
        protected ArrayList<PlaceLocation> doInBackground(String... jsonData) {

            JSONObject jObject;

            try {
                jObject = new JSONObject(jsonData[0]);

                PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
                // Getting the parsed data as a List construct
                locationList = placeDetailsJsonParser.getLocation(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return locationList;
        }

        @Override
        protected void onPostExecute(ArrayList<PlaceLocation> result) {

            PlaceLocation hm = result.get(0);

            setUpMapIfNeeded(hm.getLatitude(), hm.getLongitude(), 12);

            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location=" + hm.getLatitude() + "," + hm.getLongitude());
            sb.append("&radius=10000");
            sb.append("&types=" + "restaurant");
            sb.append("&sensor=true");
            sb.append("&key=" + getString(R.string.browser_key));

            // Creating a new non-ui thread task to download json data
            PlacesTask placesTask = new PlacesTask();

            // Invokes the "doInBackground()" method of the class PlaceTask
            placesTask.execute(sb.toString());

//            if (Utils.isInternetConnected(SearchLocationActivity.this)) {
//                new LoadPlaces(latitude, longitude).execute();
//            } else {
//                alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
//            }
        }
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
            RestaurentParserTask parserTask = new RestaurentParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class RestaurentParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                Log.d(TAG, "Restaurents : " + jsonData[0].toString());
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
            mMap.clear();

            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                SearchHotel hotel = new SearchHotel(lat,lng,name + " : " + vicinity);
                array_Hotel.add(hotel);
                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);

                // set marker icon
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_hotel_location));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
                mMap.setOnInfoWindowClickListener(MarkerrrClickListener);
            }
        }
    }
    private GoogleMap.OnInfoWindowClickListener MarkerrrClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
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

}
