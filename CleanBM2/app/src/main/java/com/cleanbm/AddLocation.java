package com.cleanbm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.Utils.TextUtils;
import com.Utils.Utils;
import com.adapter.PopupMenuAdapter;
import com.dialog.AlertDialogManager;
import com.javabeans.ImagesBean;
import com.javabeans.Popup_Menu_Item;
import com.parse.GetCallback;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.widgets.HorizontalListView;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.ui.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Ratufa.Paridhi on 8/13/2015.
 *  We can add location in the parse
 */
public class AddLocation extends Activity {
    private RatingBar rate_BathRoom;
    private RadioGroup radioGroup;
    private EditText edtBathRoomDescrip;
    private ScrollView scrollView;
    private TextView txtAddThisLocation;
    private ImageView ImageView_photo;
    private String rating_value, userName;
    private String BathRoomtype = "Squat", user_Id, BathRoomDescription;
    private String TAG = "AddNewLocation";
    int imageCount = 1;
    private float rating;
    private TextView txt_Titlebar;
    private double Gps_lat;
    private double Gps_lon;
    private EditText edtLocationName;
    private ArrayList<ImagesBean> imagesList = new ArrayList<ImagesBean>();
    private HorizontalListView hrzListView;
    private ImageView img_navigation_icon, img_Menu;
    PopupMenuAdapter adapter;
    PopupWindow popupWindow;
    private AlertDialogManager alert = new AlertDialogManager();
    private PhotoAdapter1 photoAdapter1;
    int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE=101;

    /*
        When we press on Backbutton icon or back button, it finish the current activity
    */
    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == img_navigation_icon) {
                onBackPressed();
            }
        }
    };

    /*
    Add the location to the parse database
    */
    View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v == txtAddThisLocation) {
                // Get the bathroom description from edit text
                BathRoomDescription = edtBathRoomDescrip.getText().toString().trim();
                // Get the bathroom rating from rating bar
                float rate = rate_BathRoom.getRating();
                Log.e(TAG, " rating " + rate);

                if (BathRoomDescription.equals("")) {
                    alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.enter_bath_description));
                } else if (TextUtils.isNullOrEmpty(edtLocationName.getText().toString())) {
                    alert.showAlertDialog(AddLocation.this, "Please enter location name.");
                } else if (rate_BathRoom.getRating() == 0.0) {
                    alert.showAlertDialog(AddLocation.this, "Please give rating");
                } else {
                    if (Utils.isInternetConnected(AddLocation.this)) {
                        // Get the current user detail from parse
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        Log.e(TAG, " " + currentUser.getUsername());
                        if (!TextUtils.isNullOrEmpty(currentUser.getUsername())) {
                            // If current user id login then add the location to the parse database
                            userName = currentUser.getString("name");
                            // Getting current user Object id
                            user_Id = currentUser.getObjectId();
                            // Calling the Async class which put all the data to the parse Table
                            HandlerAsync handlerAsync = new HandlerAsync();
                            handlerAsync.execute(edtLocationName.getText().toString());
                        } else {
                            /*
                                If user is not Login, Then this pop up will be open to show Login First.
                            */
                            Log.e(TAG, "please login");
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddLocation.this);
                            // Setting Dialog Message
                            alertDialog.setMessage(getResources().getString(R.string.login_first_message));
                            // Setting Positive "Yes" Button
                            alertDialog.setPositiveButton("Login",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //finish();
                                            String bathDes = edtBathRoomDescrip.getText().toString();
                                            float rating = rate_BathRoom.getRating();
                                            Log.d(TAG, " detail on click on login " + BathRoomDescription + " " + rating + " " + BathRoomtype);
                                            /*
                                                Store all the exisiting data filled in the form
                                                and intent to the LoginActivity
                                            */
                                            Intent in = new Intent(AddLocation.this, LoginActivity.class);
                                            in.putExtra("BathDescription", bathDes);
                                            in.putExtra("BathRating", rating);
                                            in.putExtra("BathType", BathRoomtype);
                                            startActivityForResult(in, 101);
                                        }
                                    });
                            // Setting Negative "NO" Button
                            alertDialog.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Write your code here to invoke NO event
                                            dialog.cancel();
                                        }
                                    });
                            // Showing Alert Message
                            alertDialog.show();
                        }
                    } else {
                        // When internet is not present, this else block will be run.
                        alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.connection_not_available));
                    }
                }
            } else if (v == ImageView_photo) {
                /*
                    TO select multiple images for upload.
                    Library used to select multiple Images.
                */
               /* new Picker.Builder(AddLocation.this, new MyPickListener(), R.style.MIP_theme)
                        .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                        .setLimit(10)
                        .build()
                        .startActivity();*/

                selectImage();

               /* new Picker.Builder(AddLocation.this,new MyPickListener(),R.style.MIP_theme)
                        .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                        .setLimit(10)
                        .build()
                        .startActivity();*/
            }
        }
    };
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(AddLocation.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // start the image capture Intent
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                } else if (items[item].equals("Choose from Gallery")) {
                    new Picker.Builder(AddLocation.this, new MyPickListener(), R.style.MIP_theme)
                            .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                            .setLimit(10)
                            .build()
                            .startActivity();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
   // - See more at: http://www.theappguruz.com/blog/android-take-photo-camera-gallery-code-sample#sthash.b6zO98qS.dpuf


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        {
           String path=(String) data.getExtras().get("data");
            ImagesBean imagesBean = new ImagesBean();

            imagesBean.setUri(Uri.fromFile(new File(path)));
            imagesList.add(imagesBean);
            photoAdapter1.notifyDataSetChanged();
        }
    }

    private class MyPickListener implements Picker.PickListener {
        @Override
        public void onPickedSuccessfully(final ArrayList<ImageEntry> images) {
            for (int i = 0; i < images.size(); i++) {
                Log.d(TAG, "Image Path : " + images.get(i).path + "\n");
                // Setting the Image Path in the JavaBeans(ImageBean) and adding in the arraylist imageList
                ImagesBean imagesBean = new ImagesBean();
                imagesBean.setUri(Uri.fromFile(new File(images.get(i).path)));
                imagesList.add(imagesBean);
            }
            // PhotoAdapter is custom adapter for showing images on the Horizontal listview.
            photoAdapter1 = new PhotoAdapter1();
            hrzListView.setAdapter(photoAdapter1);
        }

        @Override
        public void onCancel() {
            // User cancled the pick activity
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

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
        txt_Titlebar.setText("Add Location");
        /*
            Navigation (menu icon) visiblity on and set the on click listener on menu.
       */
        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);

        /*
            Setting on click listener on the Select Photo option at the bottom in the form.
        */
        ImageView_photo = (ImageView) findViewById(R.id.image_photo);
        ImageView_photo.setOnClickListener(mButtonClickListener);

        hrzListView = (HorizontalListView) findViewById(R.id.hrzListView);
        /*
            When i click on the images selected from library, cross icon will be appear.
            and when we click on cross icon, it remove from the arraylist imageList
            and notify the adapter
        */
        hrzListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final ImageView ivCross = (ImageView) view.findViewById(R.id.ivCross);
                ivCross.setVisibility(View.VISIBLE);
                ivCross.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imagesList.remove(position);
                        photoAdapter1.notifyDataSetChanged();
                    }
                });
                return false;
            }
        });

        /*
            Setting the id of Rating bar and set on CLick listener on the rating bar.
            Getting value from the rating bar
        */
        rate_BathRoom = (RatingBar) findViewById(R.id.ratingBathRoom);
        rate_BathRoom.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating_value = String.valueOf(ratingBar.getRating());
            }
        });

        /*
            Setting the id of radio group
            and get the value from radio button
        */
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_Squat) {
                    BathRoomtype = "Squat";
                } else if (checkedId == R.id.radio_Sit) {
                    BathRoomtype = "Sit";
                }
            }
        });

        /*
            setting the id of bathroom description edit text
            and after enter bathroom description,keyboard hide.
        */
        edtBathRoomDescrip = (EditText) findViewById(R.id.edtBathRoomDescription);
        Utils.hideKeyBoard(getApplicationContext(), edtBathRoomDescrip);
        /*
            Setting the Location Name from the Intent.
            Add new Location class pass the address and seting here.
        */
        edtLocationName = (EditText) findViewById(R.id.edtLocationName);
        edtLocationName.setText(getIntent().getStringExtra("Address"));

        txtAddThisLocation = (TextView) findViewById(R.id.txt_AddLocation);
        txtAddThisLocation.setOnClickListener(mButtonClickListener);

        ParseObject parseObject = new ParseObject("User");
        parseObject.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
                if (e == null) {
                    String user_id = parseObject.getString("objectId");
                    Log.e(TAG, "user_id" + user_id);
                } else {
                    Log.e(TAG, "Error" + e.getLocalizedMessage());
                }
            }

        });

        /*
            Parent scroll view can able to scroll.
       */
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtBathRoomDescrip.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        edtBathRoomDescrip.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtBathRoomDescrip.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
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
        adapter = new PopupMenuAdapter(AddLocation.this, R.layout.popup_menu_item, menus);
        //the drop down list in a listview
        ListView lstMenu = new ListView(AddLocation.this);
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
            AddLocation mainActivity = ((AddLocation) mContext);

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
                if (Utils.isInternetConnected(AddLocation.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If internet is not present.
                    alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.connection_not_available));
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
                if (Utils.isInternetConnected(AddLocation.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddLocation.this);
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
                    alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.connection_not_available));
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

        /*
            Adding all the detail of location in parse table
        */
    public class HandlerAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utils.setProgress(AddLocation.this, true);
          //  setProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            Bundle bundle = getIntent().getExtras();
            Gps_lat = bundle.getDouble("Latitude");
            Gps_lon = bundle.getDouble("Longitude");
//            full_address = bundle.getString("Address");
            ParseGeoPoint point = new ParseGeoPoint(Gps_lat, Gps_lon);

            rating = Float.parseFloat(rating_value);

            // Adding all the detail in BathRoom Detail table
            final ParseObject parseObject = new ParseObject("BathRoomDetail");
            parseObject.put("bathRating", rating);
            parseObject.put("description", BathRoomDescription);
            parseObject.put("bathRoomType", BathRoomtype);
            parseObject.put("bathLocation", point);
            parseObject.put("userId", user_Id);
            parseObject.put("userInfo", ParseUser.getCurrentUser());
          //  Log.d(TAG, " Before Parse full address" + full_address);
            parseObject.put("bathFullAddress", params[0]);
            parseObject.put("approve", "YES");

            Log.e(TAG, rating_value + "" + BathRoomDescription + " " + BathRoomtype + " ");

            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {

                    if (e == null) {
                        // If Bath Room detail is successfully added then RatingByUser table is filled by review.
                        String bath_room_id = parseObject.getObjectId();
                       final ParseObject parseObject1 = new ParseObject("RattingByUser");
                        parseObject1.put("bathRoomID", bath_room_id);
                        parseObject1.put("userId", user_Id);
                        parseObject1.put("MessageReview", BathRoomDescription);
                        parseObject1.put("bathRating", rating);
                        parseObject1.put("bathRoomType", BathRoomtype);
                        parseObject1.put("userName", userName);
                        parseObject1.put("userInfo", ParseUser.getCurrentUser());
                        parseObject1.put("bathInfo", ParseObject.createWithoutData("BathRoomDetail", bath_room_id));

                        parseObject1.saveInBackground();

                        if (imagesList != null) {
                            // If User pick the images from gallery or camera, then it goes to BathroomImages table
                            for (int i = 0; i < imagesList.size(); i++) {
                                try {
                                    Log.d(TAG, " image url" + imagesList.get(i).getUri());
                                    Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(imagesList.get(i).getUri()));
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    // get byte array here
                                    byte[] bytearray = stream.toByteArray();

                                    long time = System.currentTimeMillis();
                                    String sImageName = user_Id + bath_room_id + time + ".jpeg";
                                    Log.d("Image Name : ", sImageName);

                                    // Create the ParseFile
                                    ParseFile file = new ParseFile(sImageName, bytearray);
                                    // Upload the image into Parse Cloud
                                    //.saveIn();
                                  final  ParseObject parseObject = new ParseObject("BathroomImages");
                                    // Create a column named "ImageName" and set the string
                                    parseObject.put("bathroomID", bath_room_id);
                                    parseObject.put("userId", user_Id);
                                    // Create a column named "ImageFile" and insert the image
                                    parseObject.put("bathroomImage", file);
                                    parseObject.put("approve", "YES");
                                    parseObject.put("userInfo", ParseUser.getCurrentUser());
                                    parseObject.put("bathInfo", ParseObject.createWithoutData("BathRoomDetail", bath_room_id));

                                    // Create the class and the columns
                                    parseObject.saveInBackground();
                                    imageCount += 1;
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    } else {
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Utils.setProgress(AddLocation.this,false);
           // setProgress(false);
            edtBathRoomDescrip.setText("");
            rate_BathRoom.setRating(0);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddLocation.this);
            // Setting Dialog Title
            alertDialog.setTitle("Thank you for your Submission.");
            // Setting Dialog Message
         //   alertDialog.setMessage("Your location has been added and awaiting approval from the CleanBM Team.  You will receive a notification once it is approved.");
            alertDialog.setMessage("Your location has been added successfully.");

            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent in = new Intent();
                            setResult(111, in);
                            finish();
                        }
                    });
            // Showing Alert Message
            alertDialog.show();
        }
    }

    /*
        PhotoAdapter for Showing photos on the horizontal listview.
    */
    class PhotoAdapter1 extends BaseAdapter {

        private LayoutInflater inflater = null;
        private ViewHolder viewHolder;

        public PhotoAdapter1() {
            inflater = (LayoutInflater) AddLocation.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return imagesList.size();
        }

        @Override
        public Object getItem(int position) {
            return imagesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImagesBean bean = (ImagesBean) getItem(position);
            try {
                viewHolder = new ViewHolder();
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_imageview, null);
                    viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.imageView);
                    viewHolder.ivCross = (ImageView) convertView.findViewById(R.id.ivCross);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.mImageView.setImageURI(bean.getUri());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        public class ViewHolder {
            private ImageView mImageView, ivCross;
        }
    }
}
