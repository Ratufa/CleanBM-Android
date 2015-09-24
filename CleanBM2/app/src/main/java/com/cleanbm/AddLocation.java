package com.cleanbm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.widgets.HorizontalListView;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by Ratufa.Paridhi on 8/13/2015.
 */
public class AddLocation extends Activity {
    private RatingBar rate_BathRoom;
    private RadioGroup radioGroup;
    private EditText edtBathRoomDescrip;
    private ScrollView scrollView;
    private TextView txtAddThisLocation;
    private ImageView ImageView_photo;
    private String rating_value, userName;
    private String BathRoomtype = "Squat", user_Id, full_address, BathRoomDescription;
    private String TAG = "AddNewLocation";
    int imageCount = 1;
    private float rating;
    private LinearLayout layout_imageCLick;
    private TextView txt_Titlebar;
    private double Gps_lat;
    private double Gps_lon;
    private EditText edtLocationName;
    private ArrayList<ImagesBean> imagesList = new ArrayList<ImagesBean>();
    private HorizontalListView hrzListView;
    private ImageView img_navigation_icon, img_Menu; //,img_Cancel;
    PopupMenuAdapter adapter;
    PopupWindow popupWindow;

    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == img_navigation_icon) {
                onBackPressed();
            }
        }
    };
    private AlertDialogManager alert = new AlertDialogManager();

    View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v == txtAddThisLocation) {
                BathRoomDescription = edtBathRoomDescrip.getText().toString().trim();
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
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        Log.e(TAG, " " + currentUser.getUsername());
                        if (!TextUtils.isNullOrEmpty(currentUser.getUsername())) {
                            userName = currentUser.getString("name");
                            user_Id = currentUser.getObjectId();
                            HandlerAsync handlerAsync = new HandlerAsync();
                            handlerAsync.execute(edtLocationName.getText().toString());
                        } else {
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
                            //      alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.login_first_message));
                        }
                    } else {
                        alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.connection_not_available));
                    }
                    // setProgress(false);
                }
            } else if (v == ImageView_photo) {
                new Picker.Builder(AddLocation.this, new MyPickListener(), R.style.MIP_theme)
                        .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                        .setLimit(10)
                        .build()
                        .startActivity();
            }
        }
    };

    private class MyPickListener implements Picker.PickListener {
        @Override
        public void onPickedSuccessfully(final ArrayList<ImageEntry> images) {
            for (int i = 0; i < images.size(); i++) {
                Log.d(TAG, "Image Path : " + images.get(i).path + "\n");
                ImagesBean imagesBean = new ImagesBean();
                imagesBean.setUri(Uri.fromFile(new File(images.get(i).path)));
                imagesList.add(imagesBean);
            }
            photoAdapter1 = new PhotoAdapter1();
            hrzListView.setAdapter(photoAdapter1);
        }

        @Override
        public void onCancel() {
            // User cancled the pick activity
        }
    }

    private ProgressDialog pd;
    private PhotoAdapter1 photoAdapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
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
        txt_Titlebar.setText("Add Location");

        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);

        layout_imageCLick = (LinearLayout) findViewById(R.id.layout_imageCLick);

        ImageView_photo = (ImageView) findViewById(R.id.image_photo);
        ImageView_photo.setOnClickListener(mButtonClickListener);

        hrzListView = (HorizontalListView) findViewById(R.id.hrzListView);
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

        rate_BathRoom = (RatingBar) findViewById(R.id.ratingBathRoom);
        rate_BathRoom.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating_value = String.valueOf(ratingBar.getRating());
            }
        });

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

        edtBathRoomDescrip = (EditText) findViewById(R.id.edtBathRoomDescription);
        Utils.hideKeyBoard(getApplicationContext(), edtBathRoomDescrip);

        edtLocationName = (EditText) findViewById(R.id.edtLocationName);
        edtLocationName.setText(getIntent().getStringExtra("Address"));

        txtAddThisLocation = (TextView) findViewById(R.id.txt_AddLocation);
        txtAddThisLocation.setOnClickListener(mButtonClickListener);

        ParseObject parseObject = new ParseObject("User");
        parseObject.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    String user_id = parseObject.getString("objectId");
                    Log.e(TAG, "user_id" + user_id);
                } else {
                    Log.e(TAG, "Error" + e.getLocalizedMessage());
                }
            }
        });

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
        LayoutInflater inflater = (LayoutInflater) AddLocation.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[] = new Popup_Menu_Item[5];
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
        adapter = new PopupMenuAdapter(AddLocation.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(AddLocation.this);
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

        String TAG = "AddLocation.java";

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

            Popup_Menu_Item popup_menu_item = new Popup_Menu_Item();
            // String data=popup_menu_item.title;
            // String data=parent.getItemAtPosition(position).toString();

            Popup_Menu_Item info = (Popup_Menu_Item) parent.getItemAtPosition(position);
            String data = info.title;

            Log.e("Tag", data);
            if (data.equals(getString(R.string.Home))) {
                finish();
            } else if (data.equals(getString(R.string.search_near_me))) {
                if (Utils.isInternetConnected(AddLocation.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alert.showAlertDialog(AddLocation.this, getResources().getString(R.string.connection_not_available));
                }

            } else if (data.equals(getString(R.string.search_location))) {
                Intent intent = new Intent(getApplicationContext(), AddLocation.class);
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

    public void setProgress(boolean visibility) {
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(AddLocation.this);
                    pd.setTitle("");
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
                if (pd.isShowing())
                    pd.dismiss();
            } catch (Exception e) {
                Utils.sendExceptionReport(e, getApplicationContext());
                e.printStackTrace();
            }
        }
    }

    public class HandlerAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            Bundle bundle = getIntent().getExtras();
            Gps_lat = bundle.getDouble("Latitude");
            Gps_lon = bundle.getDouble("Longitude");
//            full_address = bundle.getString("Address");
            ParseGeoPoint point = new ParseGeoPoint(Gps_lat, Gps_lon);

            rating = Float.parseFloat(rating_value);

            final ParseObject parseObject = new ParseObject("BathRoomDetail");
            parseObject.put("bathRating", rating);
            parseObject.put("description", BathRoomDescription);
            parseObject.put("bathRoomType", BathRoomtype);
            parseObject.put("bathLocation", point);
            parseObject.put("userId", user_Id);
            parseObject.put("userInfo", ParseUser.getCurrentUser());
            Log.d(TAG, " Before Parse full address" + full_address);
            parseObject.put("bathFullAddress", params[0]);
            parseObject.put("approve", "YES");

            Log.e(TAG, rating_value + "" + BathRoomDescription + " " + BathRoomtype + " ");

            parseObject.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {

                        String bath_room_id = parseObject.getObjectId();
                       final ParseObject parseObject1 = new ParseObject("RattingByUser");
                        parseObject1.put("bathRoomID", bath_room_id);
                        parseObject1.put("userId", user_Id);
                        parseObject1.put("MessageReview", BathRoomDescription);
                        parseObject1.put("bathRating", rating);
                        parseObject1.put("bathRoomType", BathRoomtype);
                        parseObject1.put("userName", userName);
                        parseObject1.put("userInfo", ParseUser.getCurrentUser());
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("BathRoomDetail");
                        query.getInBackground(bath_room_id, new GetCallback<ParseObject>() {
                            public void done(ParseObject object, ParseException e) {
                                if (e == null) {
                                    Log.d(TAG," Get bathroom details "+object.getString("bathRating")+object.getString("description")+object.getObjectId());
                                    parseObject1.put("bathInfo", object);
                                } else {
                                    // something went wrong
                                }
                            }
                        });
                       // ParseObject myBathRoomDetailPtr = ParseObject.createWithoutData("BathRoomDetail", parseObject.getObjectId());

                        parseObject1.saveInBackground();

                        if (imagesList != null) {
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
                                    query.getInBackground(bath_room_id, new GetCallback<ParseObject>() {
                                        public void done(ParseObject object, ParseException e) {
                                            if (e == null) {
                                                parseObject.put("bathInfo", object);
                                            } else {
                                                // something went wrong
                                            }
                                        }
                                    });

                                    // Create the class and the columns
                                    parseObject.saveInBackground();
                                    imageCount += 1;
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    } else {
                        //   myObjectSaveDidNotSucceed();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setProgress(false);
            edtBathRoomDescrip.setText("");
            rate_BathRoom.setRating(0);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddLocation.this);
            // Setting Dialog Title
            alertDialog.setTitle("Thank you for your Submission.");
            // Setting Dialog Message
            alertDialog.setMessage("Your location has been added and awaiting approval from the CleanBM Team.  You will receive a notification once it is approved.");
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
