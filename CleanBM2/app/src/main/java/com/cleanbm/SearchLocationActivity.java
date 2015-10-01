package com.cleanbm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import net.yazeed44.imagepicker.util.Util;

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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ratufa.Paridhi on 8/12/2015.
 *  SearchLocation is the Home screen in which there are two options
 *  1) Get Nearest Bathroom , hotel and restaurants
 *  2) Advance Search field , places are get from the google api
 */
public class SearchLocationActivity extends FragmentActivity {

    private String TAG = "SearchLocationActivity";
    private ArrayList<String> contactList;
    private AutoCompleteTextView autoCompView;
    private TextView txtLookUpThisLocation;
    double restaurentLatitude, restaurentLongitude;
    private ImageView img_near_me,img_navigation_icon, img_Menu;
    PopupWindow popupWindow;
    private AlertDialogManager alert = new AlertDialogManager();
    public static int flag = 0;
    public int get_place_from_autocomplete=0;
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

    // for google place
    private DownloadTask placesDownloadTask;
    private DownloadTask placeDetailsDownloadTask;
    private ParserTask placesParserTask;
    private DetailParserTask placeDetailsParserTask;
    final int PLACES = 0;
    TextView txt_Titlebar;
    final int PLACES_DETAILS = 1;
    String term,reference;

    public static final String LOGIN_PREFERENCES = "my_preferences";

    private AddressBean address = new AddressBean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
       // Indore
        contactList = new ArrayList<String>();
        autoCompView = (AutoCompleteTextView) findViewById(R.id.edtSearchLocation);
        autoCompView.setThreshold(1);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this,R.layout.spinner_item));
        //   autoCompView.setAdapter(new PlacesAutoCompleteAdapter(SearchLocationActivity.this,android.R.layout.simple_list_item_1));
        /*autoCompView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(placesDownloadTask!=null)
                {
                    if(placesDownloadTask.getStatus() == AsyncTask.Status.PENDING ||
                            placesDownloadTask.getStatus() == AsyncTask.Status.RUNNING ||
                            placesDownloadTask.getStatus() == AsyncTask.Status.FINISHED)
                    {
                        Log.i("--placesDownloadTask--","progress_status : "+placesDownloadTask.getStatus());
                        placesDownloadTask.cancel(true);
                    }
                }
             *//*   //    handleIntent(getIntent());
                // shouldAutoComplete = true;
                // Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GooglePlaces.getAutoCompleteUrl(SearchLocationActivity.this, s.toString());
                Log.d("URL : ", url);

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);*//*
            }

            @Override
            public void afterTextChanged(Editable s) {
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
        });*/

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });

        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                get_place_from_autocomplete=1;
               term = resultList.get(position).getDescription();
                reference = resultList.get(position).getReference();

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);
                Log.d(TAG," Log 1"+term+" "+reference);

                // Getting url to the Google Places details api
                String url = GooglePlaces.getPlaceDetailsUrl(SearchLocationActivity.this, reference);

                Log.d(TAG, "Detail URL :" + url);
              //  Utils.setProgress(SearchLocationActivity.this,true);
                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);
                autoCompView.setText(term);
              //  Utils.setProgress(SearchLocationActivity.this,false);
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
                            if(get_place_from_autocomplete==1) {
                                flag = 1;
                                Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                                intent.putExtra("R_LAT", restaurentLatitude);
                                intent.putExtra("R_LON", restaurentLongitude);
                                Log.d(TAG, " before pass" + restaurentLatitude + " " + restaurentLongitude);
                                startActivity(intent);
                                autoCompView.setText("");
                                get_place_from_autocomplete=0;
                            }
                            else
                            {
                                alert.showAlertDialog(SearchLocationActivity.this,"Unable to find this location.Please modified your location.");
                                Log.d(TAG," Show message : User enter the full address!!");
                            }
                } else {
                    alert.showAlertDialog(SearchLocationActivity.this, getResources().getString(R.string.connection_not_available));
                }
            }

        });

    }



    private boolean fbUser = false;
    Boolean email_verify;
    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LayoutInflater inflater = (LayoutInflater) SearchLocationActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] = new Popup_Menu_Item[5];
        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
        email_verify = currentUser.getBoolean("emailVerified");
        Log.d("Splash screen "," "+email_verify);
        if ((currentUser.getUsername() != null && email_verify==true) || fbUser) {
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
                in.putExtra("BathDescription", "");
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
                       /* SharedPreferences preferences = getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
                        // first time
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("RanBefore", false);
                        editor.commit();*/
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

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        backButtonHandler();
        return;
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
        protected void onPreExecute() {
            super.onPreExecute();
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
            //setProgress(false);
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
                    Log.d(TAG,"Log 4");
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
            // Creating a SimpleAdapter for the AutoCompleteTextView Indore airport
            SpinnerAdapter adapter = new SpinnerAdapter(SearchLocationActivity.this, result);
            // Setting the adapter
            autoCompView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    ArrayList<PlaceLocation> locationList = null;
    ProgressDialog dialog;

    private class DetailParserTask extends AsyncTask<String, Integer, ArrayList<PlaceLocation>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SearchLocationActivity.this);
            dialog.setMessage(getString(R.string.loading));
            dialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_dialog));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            Log.d(TAG, "Log 2");
        }

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

            Log.d(TAG,"log 3");
            PlaceLocation hm = result.get(0);

            // Getting latitude from the parsed data
            restaurentLatitude = hm.getLatitude();
            Log.d(TAG, "Latitude : " + restaurentLatitude);

            // Getting longitude from the parsed data
            restaurentLongitude = hm.getLongitude();
            Log.d(TAG, "Longitude : " + restaurentLongitude);
            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    dialog.dismiss(); // when the task active then close the dialog
                    t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                }
            }, 2000);
           }
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
