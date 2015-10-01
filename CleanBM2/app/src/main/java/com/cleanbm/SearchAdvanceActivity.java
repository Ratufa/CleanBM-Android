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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ratufa.Paridhi on 9/5/2015.
 *  User can search particular location and all near by hotel and restaurant show on map
 */
public class SearchAdvanceActivity extends FragmentActivity {

    // img : http://www.androidbegin.com/tutorial/android-parse-com-simple-listview-tutorial/
    //http://wptrafficanalyzer.in/blog/android-autocompletetextview-with-google-places-autocomplete-api/
    // http://stackoverflow.com/questions/23289380/this-ip-site-or-mobile-application-is-not-authorized-to-use-this-api-key-with-i
    String TAG = "SearchAdvanceActivity";
    ArrayList<String> contactList;
    ArrayList<SearchHotel> array_Hotel = new ArrayList<SearchHotel>();

    AutoCompleteTextView autoCompView;
    AlertDialogManager alert = new AlertDialogManager();
    public static int flag = 0;
    String data;

    ArrayList<SearchHotel> arrayList_hotel = new ArrayList<SearchHotel>();

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();
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
    String term,reference;
    public int get_place_from_autocomplete=0;


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
        Gps_lat = ( Double.parseDouble(latitude));
        Gps_lon = ( Double.parseDouble(longitude));
        setUpMapIfNeeded(Gps_lat, Gps_lon, 16);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.edtSearchLocation);
        Utils.hideKeyBoard(getApplicationContext(), autoCompView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.spinner_item));


       /* autoCompView.addTextChangedListener(new TextWatcher() {
            private boolean shouldAutoComplete = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //    handleIntent(getIntent());
             //  shouldAutoComplete = true;
                Log.d(TAG," on text changed"+s.toString());
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GooglePlaces.getAutoCompleteUrl(SearchAdvanceActivity.this, s.toString());
                Log.d("URL : ", url);

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG," After text changes"+s.toString());
              *//*  placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GooglePlaces.getAutoCompleteUrl(SearchAdvanceActivity.this, s.toString());
                Log.d("URL : ", url);

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);*//*

            }
        });
*/

        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                get_place_from_autocomplete=1;

                Utils.hideKeyBoard(SearchAdvanceActivity.this, autoCompView);
                term = resultList.get(position).getDescription();
                reference = resultList.get(position).getReference();

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);
                Log.d(TAG," Log 1"+term+" "+reference);

                // Getting url to the Google Places details api
                String url = GooglePlaces.getPlaceDetailsUrl(SearchAdvanceActivity.this, reference);

                Log.d(TAG, "Detail URL :" + url);
                //  Utils.setProgress(SearchLocationActivity.this,true);
                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);
                autoCompView.setText(term);


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

    Boolean fbUser=false;
    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LayoutInflater inflater = (LayoutInflater) SearchAdvanceActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] ;//= new Popup_Menu_Item[5];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
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
                in.putExtra("BathDescription", "");
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

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    //------------ make your specific key ------------
    private static final String API_KEY = "AIzaSyCJWHBdeonUF9Gafppf6Ag23NRiUhuuzoE";
    ArrayList<PlacesSuggestionsBean> resultList = null;
    public  ArrayList<PlacesSuggestionsBean> autocomplete(String input) {


        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {

            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            //sb.append("&components=country:india"); 12,Shikshak Nagar Airport Road,Indore Madhya Pradesh
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: "+url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("sdf", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("dsd", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<PlacesSuggestionsBean>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                PlacesSuggestionsBean placesSuggestionsBean = new PlacesSuggestionsBean();
                placesSuggestionsBean.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
                placesSuggestionsBean.setId(predsJsonArray.getJSONObject(i).getString("id"));
                placesSuggestionsBean.setReference(predsJsonArray.getJSONObject(i).getString("reference"));
                resultList.add(placesSuggestionsBean);
            }
        } catch (JSONException e) {
            Log.e("adsd", "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<PlacesSuggestionsBean> implements Filterable {
        private ArrayList<PlacesSuggestionsBean> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public PlacesSuggestionsBean getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() { // 12 Shikshak nagar airport road, Indore Madhaya Pradesh
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            PlacesSuggestionsBean bean = getItem(position);

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);

                holder.tvFullName = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvFullName.setText(bean.getDescription());
            return convertView;
        }

        class ViewHolder {
            private TextView tvFullName;
        }
    }


}
