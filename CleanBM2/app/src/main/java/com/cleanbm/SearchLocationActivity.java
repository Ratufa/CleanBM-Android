package com.cleanbm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.Utils.GPSTracker;
import com.Utils.Utils;
import com.adapter.AddressAdapter;
import com.adapter.PopupMenuAdapter;
import com.adapter.SpinnerAdapter;
import com.dialog.AlertDialogManager;
import com.googleplace.GooglePlaces;
import com.googleplace.PlaceDetailsJSONParser;
import com.googleplace.PlaceJSONParser;
import com.javabeans.AddressBean;
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

/**
 * Created by Ratufa.Paridhi on 8/12/2015.
 */
public class SearchLocationActivity extends FragmentActivity { //implements LoaderManager.LoaderCallbacks<Cursor> {

    private String TAG = "SearchLocationActivity";
    private ArrayList<String> contactList;
    private AutoCompleteTextView autoCompView;
    private TextView txtLookUpThisLocation;
    double restaurentLatitude, restaurentLongitude;
    private ImageView img_near_me,img_navigation_icon, img_Menu;
    PopupWindow popupWindow;
    private AlertDialogManager alert = new AlertDialogManager();
    public static int flag = 0;
    PopupMenuAdapter adapter;

    private ArrayList<SearchHotel> arrayList_hotel = new ArrayList<SearchHotel>();

    // ListItems data
    private ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();
    // HashMap<String, String>  placesListItems1 = new HashMap<String, String>();
    // Places List
    private PlacesList nearPlaces;
    // Google Places
    private GooglePlaces googlePlaces;
    private GPSTracker gpsTracker;

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name
    // String URL = "http://maps.googleapis.com/maps/api/geocode/json?address="+reference+"&sensor=false";

    // for google place
    private DownloadTask placesDownloadTask;
    private DownloadTask placeDetailsDownloadTask;
    private ParserTask placesParserTask;
    private DetailParserTask placeDetailsParserTask;
    final int PLACES = 0;
    TextView txt_Titlebar;
    final int PLACES_DETAILS = 1;

    public static final String LOGIN_PREFERENCES = "LoginPrefs";

    private AddressBean address = new AddressBean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // initialize pop up window
        popupWindow = showMenu();
        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
     //   img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
     //   img_navigation_icon.setOnClickListener(mMenuButtonClickListener);

        txt_Titlebar = (TextView) findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("Home");
        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mMenuButtonClickListener);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
        }
        if (Utils.isInternetConnected(SearchLocationActivity.this)) {
            try {
                gpsTracker = new GPSTracker(getApplicationContext());
                gpsTracker.getLocation();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        } else {
            alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
        }
        contactList = new ArrayList<String>();
        autoCompView = (AutoCompleteTextView) findViewById(R.id.edtSearchLocation);
        autoCompView.setThreshold(1);
        //   autoCompView.setAdapter(new PlacesAutoCompleteAdapter(SearchLocationActivity.this,android.R.layout.simple_list_item_1));
        autoCompView.addTextChangedListener(new TextWatcher() {
            private boolean shouldAutoComplete = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //    handleIntent(getIntent());
                // shouldAutoComplete = true;
                // Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GooglePlaces.getAutoCompleteUrl(SearchLocationActivity.this, s.toString());
                Log.d("URL : ", url);

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });

        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 autoCompView.setText(suggestionsList.get(position).getDescription());

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

                // Getting url to the Google Places details api
                String url = GooglePlaces.getPlaceDetailsUrl(SearchLocationActivity.this, suggestionsList.get(position).getReference());

                Log.d(TAG, "Detail URL :" + url);

                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);
            }
        });
        txtLookUpThisLocation = (TextView) findViewById(R.id.txtLookUpThisLocation);

        img_near_me = (ImageView) findViewById(R.id.img_near_me);
        img_near_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetConnected(SearchLocationActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                } else {
                    alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }
            }
        });


        txtLookUpThisLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoCompView.getText().length() == 0) {
                    alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.enter_Confirmpassword));
                } else if (Utils.isInternetConnected(SearchLocationActivity.this)) {

                    //if (!TextUtils.isNullOrEmpty(address.getAddress())) {
                    flag = 1;
//                        LatLng latLng = new LatLng(restaurentLatitude, restaurentLongitude);
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    intent.putExtra("R_LAT", restaurentLatitude);
                    intent.putExtra("R_LON", restaurentLongitude);
                    Log.d(TAG, " before pass" + restaurentLatitude + " " + restaurentLongitude);
                    startActivity(intent);
                    autoCompView.setText("");
                   //}
                } else {
                    alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }

            }

        });

    }

    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LayoutInflater inflater = (LayoutInflater) SearchLocationActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] = new Popup_Menu_Item[5];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        if (currentUser.getUsername() != null && email_verify==true) {
            menus = new Popup_Menu_Item[]{
                   new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.my_account)),
                    new Popup_Menu_Item(R.drawable.sign_out_button, getResources().getString(R.string.Logout)),
            };
        } else {
            menus = new Popup_Menu_Item[]{
                    new Popup_Menu_Item(R.drawable.location_icon, getResources().getString(R.string.search_near_me)),
                    new Popup_Menu_Item(R.drawable.search_icon, getResources().getString(R.string.search_location)),
                    new Popup_Menu_Item(R.drawable.add_bathroom_icon, getResources().getString(R.string.add_new_location)),
                    new Popup_Menu_Item(R.drawable.support_icon, getResources().getString(R.string.support)),
                    new Popup_Menu_Item(R.drawable.login_icon, getResources().getString(R.string.Login_menu))
            };
        }

        Log.e("Array size", menus.length + " ");
        adapter = new PopupMenuAdapter(SearchLocationActivity.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(SearchLocationActivity.this);
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


    public class DropdownOnItemClickListener implements AdapterView.OnItemClickListener {

        String TAG = "DashBoardActivity.java";

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {

            // get the context and main activity to access variables
            Context mContext = v.getContext();
            SearchLocationActivity mainActivity = ((SearchLocationActivity) mContext);

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
            if (data.equals(getString(R.string.search_near_me))) {
                if (Utils.isInternetConnected(SearchLocationActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                } else {
                    alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }

            } else if (data.equals(getString(R.string.search_location))) {
                Intent intent = new Intent(getApplicationContext(), SearchAdvanceActivity.class);
                //  startActivityForResult(intent, 101);
                startActivity(intent);
            } else if (data.equals(getString(R.string.add_new_location))) {
                Intent intent = new Intent(getApplicationContext(), AddNewLocationActivity.class);
                //   startActivity(intent);
                startActivityForResult(intent, 777);
            } else if (data.equals(getString(R.string.support))) {
                Intent intent = new Intent(getApplicationContext(), SupportActivity.class);
                startActivity(intent);
            } else if (data.equals(getString(R.string.my_account))) {
                Intent intent = new Intent(getApplicationContext(), MyAccountActivity.class);
                startActivity(intent);
            } else if (data.equals(getString(R.string.Logout))) {
                if (Utils.isInternetConnected(SearchLocationActivity.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchLocationActivity.this);
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
                    alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getResources().getString(R.string.Login_menu))) {
             //   flag_for_login = 1;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(in);
            }
        }

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchLocationActivity.this);
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to exit?");
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
                        // first time
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("RanBefore", false);
                        editor.commit();
                        finish();
                    }
                });
        // Setting Negative "NO" Button
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
                AddressAdapter adapter = new AddressAdapter(SearchLocationActivity.this, placesList);

                // Setting the adapter
                autoCompView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } else {

            }
        }
    }


//    /**
//     * Background Async Task to Load Google places
//     */
//    class LoadPlaces extends AsyncTask<String, String, String> {
//
//        Double la, ln;
//
//        public LoadPlaces(double latitude, double longitude) {
//            this.la = latitude;
//            this.ln = longitude;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        protected String doInBackground(String... args) {
//            // creating Places class object
//            googlePlaces = new GooglePlaces();
//
//            try {
//                // Separeate your place types by PIPE symbol "|"
//                // If you want all types places make it as null
//                // Check list of types supported by google
//                String types = "restaurant"; // Listing places only cafes, restaurants
//
//                // Radius in meters - increase this value if you don't find any places
//                double radius = 10000; // 1000 meters
//
//                nearPlaces = googlePlaces.search(la, ln, radius, types);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        protected void onPostExecute(String file_url) {
//            /**
//             * Updating parsed Places into LISTVIEW
//             * */
//            // Get json response status
//            String status = nearPlaces.status;
//
//            // Check for all possible status
//            if (status.equals("OK")) {
//                // Successfully got places details
//                if (nearPlaces.results != null) {
//                    // loop through each place
//                    for (Place p : nearPlaces.results) {
//                        HashMap<String, String> map = new HashMap<String, String>();
//
//                        // Place reference won't display in listview - it will be hidden
//                        // Place reference is used to get "place full details"
//                        map.put(KEY_REFERENCE, p.reference);
//
//                        String name = p.vicinity;
//                        // Place name
//                        map.put(KEY_VICINITY, p.vicinity);
//
//                        // adding HashMap to ArrayList
//                        placesListItems.add(map);
//                        contactList.add(name);
//                    }
//                    // list adapter
//                    SimpleAdapter adapter = new SimpleAdapter(SearchLocationActivity.this, placesListItems,
//                            R.layout.search_list_item,
//                            new String[]{KEY_REFERENCE, KEY_VICINITY}, new int[]{
//                            R.id.reference, R.id.name});
//
//                }
//            } else if (status.equals("ZERO_RESULTS")) {
//                alert.showAlertDialog(SearchLocationActivity.this, "Near Places Sorry no places found. Try to change the types of places");
//            } else if (status.equals("UNKNOWN_ERROR")) {
//                alert.showAlertDialog(SearchLocationActivity.this, "Places Error Sorry unknown error occured.");
//            } else if (status.equals("OVER_QUERY_LIMIT")) {
//                alert.showAlertDialog(SearchLocationActivity.this, "Places Error Sorry query limit to google places is reached");
//            } else if (status.equals("REQUEST_DENIED")) {
//                alert.showAlertDialog(SearchLocationActivity.this, "Places Error Sorry error occured. Request is denied");
//            } else if (status.equals("INVALID_REQUEST")) {
//                alert.showAlertDialog(SearchLocationActivity.this, "Places Error Sorry error occured. Invalid Request");
//            } else {
//                alert.showAlertDialog(SearchLocationActivity.this, "Places Error Sorry error occured.");
//            }
//        }
//
//    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        backButtonHandler();
        return;
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
            SpinnerAdapter adapter = new SpinnerAdapter(SearchLocationActivity.this, result);
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

            // Getting latitude from the parsed data
            restaurentLatitude = hm.getLatitude();
            Log.d(TAG, "Latitude : " + restaurentLatitude);

            // Getting longitude from the parsed data
            restaurentLongitude = hm.getLongitude();
            Log.d(TAG, "Longitude : " + restaurentLongitude);

        }
    }


}
