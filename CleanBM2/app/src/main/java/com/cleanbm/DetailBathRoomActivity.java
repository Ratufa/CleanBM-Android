package com.cleanbm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Utils.TextUtils;
import com.Utils.Utils;
import com.adapter.PopupMenuAdapter;
import com.adapter.ReviewAdapter;
import com.dialog.AlertDialogManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javabeans.BathRoomDetail;
import com.javabeans.BathroomImages;
import com.javabeans.ImagesBean;
import com.javabeans.Popup_Menu_Item;
import com.javabeans.ReviewListItem;
import com.koushikdutta.ion.Ion;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.swipemenulistview.SwipeMenu;
import com.swipemenulistview.SwipeMenuCreator;
import com.swipemenulistview.SwipeMenuItem;
import com.swipemenulistview.SwipeMenuListView;
import com.widgets.HorizontalListView;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

/**
 * Created by Ratufa.Paridhi on 8/19/2015.
 */
public class DetailBathRoomActivity extends FragmentActivity implements SwipeMenuListView.OnMenuItemClickListener {
    public TextView txtBathAddress, txtGiveReview, txtReviewNumber, txt_Titlebar;
    private String user_Id;
    private SwipeMenuListView lstReview;
    private String bath_full_address, bathroom_id,bathroom_img_id;
    String userId_who_posted_img;
    private RatingBar ratingBathRoom;
    private TextView txtAddPhotos;
    private String TAG = "DetailBathRoomActivity";
    private ArrayList<ReviewListItem> reviewListItems = new ArrayList<ReviewListItem>();
    private double avg_rating = 0.0;
    private int reportCount = 0;
    private int reviewLike = 0;
    private ReviewAdapter reviewAdapter;
 //   TextView txt_LeftImgButton,txt_RightImgButton;
    PopupMenuAdapter adapter;
    PopupWindow popupWindow;

    private HorizontalListView hrzListView;

    SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // create "open" item
            SwipeMenuItem openItem = new SwipeMenuItem(
                    getApplicationContext());
            // set item background
            openItem.setBackground(new ColorDrawable(Color.RED));
            // set item width
            openItem.setWidth(dp2px(90));
            // set item title
            openItem.setTitle("Report");
            // set item title fontsize
            openItem.setTitleSize(18);

            // set item title font color
            openItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(openItem);

            // create "open" item
            SwipeMenuItem openItem1 = new SwipeMenuItem(
                    getApplicationContext());
            // set item background
            openItem1.setBackground(new ColorDrawable(Color.BLUE));
            // set item width
            openItem1.setWidth(dp2px(90));

            // set item title
            openItem1.setTitle("Like");
            // set item title fontsize
            openItem1.setTitleSize(18);

            // set item title font color
            openItem1.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(openItem1);
        }
    };
    private SupportMapFragment mMapFragment;
    private AlertDialogManager alert = new AlertDialogManager();
    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == img_navigation_icon) {
                finish();
            }
           /* else if (v == txtAddPhotos) {
                if (Utils.isInternetConnected(DetailBathRoomActivity.this)) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser.getUsername() != null) {
                        getImages();
                    } else {
                        //alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.login_first_message));
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                DetailBathRoomActivity.this);
                        // Setting Dialog Title
                        //  alertDialog.setTitle("Leave application?");
                        // Setting Dialog Message
                        alertDialog.setMessage(getResources().getString(R.string.login_first_message));
                        // Setting Icon to Dialog
                        //alertDialog.setIcon(R.drawable.dialog_icon);
                        // Setting Positive "Yes" Button
                        alertDialog.setPositiveButton("Login",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //finish();
                                        //String bathDes= edtBathRoomDescrip.getText().toString();
                                        //float rating = rate_BathRoom.getRating();
                                        //  Log.d(TAG," detail on click on login "+" "+rating+" "+BathRoomtype);

                                        Intent in = new Intent(DetailBathRoomActivity.this, LoginActivity.class);
                                        // Bundle bundle = new Bundle();
                                        //  in.putExtra("BathDescription",bathDes);
                                        in.putExtra("BathRating", "");
                                        //  in.putExtra("BathType",BathRoomtype);
                                        // bundle.putString("BathDescription", bathDes);
                                        // bundle.putFloat("BathRating", rating);
                                        // bundle.putString("BathType", BathRoomtype);
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
                    alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.connection_not_available));
                }
            }*/

        }
    };

    private ImageView  img_navigation_icon;
    private GoogleMap mMap;
    private ImageView img_Menu;
    private ProgressDialog pd;
    BathRoomDetail bathRoomDetail;
    TextView txtReportBathroom;
    String review_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_bathroom);
        // initialize pop up window
        popupWindow = showMenu();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });

        bathRoomDetail = (BathRoomDetail) getIntent().getSerializableExtra("DATA");
        if (bathRoomDetail == null) {
            finish();
        }

        txtBathAddress = (TextView) findViewById(R.id.txtBathAddress);

        txtReportBathroom =(TextView)findViewById(R.id.txtReportBathroom);
        txtReportBathroom.setPaintFlags(txtReportBathroom.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        txtReportBathroom.setOnClickListener(mReportBathRoomClick1);

        ratingBathRoom = (RatingBar) findViewById(R.id.ratingBathRoom);
        ratingBathRoom.setFocusable(false);

        txtGiveReview = (TextView) findViewById(R.id.txtGiveReview);
        reviewAdapter = new ReviewAdapter(DetailBathRoomActivity.this, reviewListItems);

        lstReview = (SwipeMenuListView) findViewById(R.id.lstReview);
        lstReview.setAdapter(reviewAdapter);
        lstReview.setExpanded(true);
        lstReview.setMenuCreator(creator);
        lstReview.setOnMenuItemClickListener(this);
        lstReview.setCloseInterpolator(new BounceInterpolator());
        lstReview.setOpenInterpolator(new BounceInterpolator());

        txtAddPhotos = (TextView) findViewById(R.id.txtAddPhotos);
        txtAddPhotos.setOnClickListener(mMenuButtonClickListener);

        txtReviewNumber = (TextView) findViewById(R.id.txtReviewNumber);

        img_Menu = (ImageView) findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.VISIBLE);
        img_Menu.setOnClickListener(mNavigationClickListener);

        txt_Titlebar = (TextView) findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("Bathroom Detail");

        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
        img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
        img_navigation_icon.setOnClickListener(mMenuButtonClickListener);

        double lat = bathRoomDetail.getLat();
        double longg = bathRoomDetail.getLongg();

        bath_full_address = bathRoomDetail.getBath_full_address();
        bathroom_id = bathRoomDetail.getBath_id();
        Log.d(TAG, " bath room id" + bathroom_id);

        setUpMapIfNeeded(lat, longg);

        txtGiveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                Boolean email_verify = currentUser.getBoolean("emailVerified");
                fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
                if ((!TextUtils.isNullOrEmpty(currentUser.getUsername()) && email_verify == true)|| fbUser) {
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            Utils.setProgress(DetailBathRoomActivity.this,true);
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("RattingByUser");
                            query.whereMatches("bathRoomID",bathroom_id);
                            query.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> list, ParseException e) {
                                    int flag = 0;
                                    String message = "";
                                    double rating =0.0;
                                    String bathroomType = "";
                                    String genderType = "";
                                    user_Id = ParseUser.getCurrentUser().getObjectId();
                                    Log.d(TAG," review btn "+user_Id+" "+bathroom_id); // 8MqH4iqZgP nRDMEk1BtA
                                    for (int i = 0; i < list.size(); i++) {
                                        String user_id_ = list.get(i).getString("userId");
                                        Log.d(TAG," user id inside"+user_id_);
                                        String bathroom_id_ = list.get(i).getString("bathRoomID");
                                        if (user_id_.equals(user_Id) && bathroom_id_.equals(bathroom_id)) {
                                            flag = 1;
                                            Log.d(TAG," matchedddd");
                                            message = list.get(i).getString("MessageReview");
                                            rating = list.get(i).getDouble("bathRating");
                                            bathroomType = list.get(i).getString("bathRoomType");
                                            genderType = list.get(i).getString("genderType");
                                            review_id = list.get(i).getObjectId();

                                        }
                                    }
                                    if (flag == 1) {
                                        Log.d(TAG, " match");

                                        Toast.makeText(getApplicationContext(),"You have already give review on this bathroom",Toast.LENGTH_SHORT).show();

                                       /* Intent in = new Intent(getApplicationContext(), GiveReviewActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("bath_id", bathroom_id);
                                        bundle.putString("update", "update");
                                        bundle.putString("message", message);
                                        bundle.putString("rating", String.valueOf(rating));
                                        bundle.putString("review_Id", review_id);
                                        bundle.putString("bathroomType", bathroomType);
                                        bundle.putString("genderType", genderType);
                                        in.putExtras(bundle);
                                        Log.d(TAG, " " + message + " " + rating + " " + bathroomType + " " + genderType);
                                        startActivityForResult(in, 102);*/
                                    } else {
                                        Intent in = new Intent(getApplicationContext(), GiveReviewActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("bath_id", bathroom_id);
                                        bundle.putString("update", "Not_update");
                                        in.putExtras(bundle);
                                        startActivityForResult(in, 102);
                                    }
                                }
                            });
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Utils.setProgress(DetailBathRoomActivity.this, false);
                        }
                    }.execute();

                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            DetailBathRoomActivity.this);
                    alertDialog.setMessage(getResources().getString(R.string.login_first_message));
                    alertDialog.setPositiveButton("Login",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent in = new Intent(DetailBathRoomActivity.this, LoginActivity.class);
                                    in.putExtra("BathRating", "");
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
            }
        });


        hrzListView = (HorizontalListView) findViewById(R.id.hrzListView);
        //horizontalGalleryView = (LinearLayout) findViewById(R.id.horizontalGallery);

        //txtBathTitle.setText(bath_name);
        txtBathAddress.setText(bath_full_address);

        if (Utils.isInternetConnected(DetailBathRoomActivity.this)) {
            displayData();
        } else {
            alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.connection_not_available));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    ArrayList<BathroomImages> arrayList_bathroomImg = new ArrayList<BathroomImages>();
    PhotoAdapter1 photoAdapter1;

    public void showBathRoomImages() {
        // Locate the class table named "ImageUpload" in Parse.com
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("BathroomImages");
        query.whereMatches("bathroomID", bathroom_id);
        query.whereMatches("approve", "YES");
        Log.e(TAG, "Bath room id!!!!!" + bathroom_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                // horizontalGalleryView.removeAllViews();
                arrayList_bathroomImg.clear();
                for (ParseObject parseObject : list) {
                    ParseFile postImage = parseObject.getParseFile("bathroomImage");
                    bathroom_img_id = parseObject.getObjectId();
                    userId_who_posted_img = parseObject.getString("userId");
                    Log.d(TAG, "bathroom id and user id who posted" + bathroom_img_id + " " + userId_who_posted_img);
                    if (postImage != null) {
                        Uri imageUri = Uri.parse(postImage.getUrl());

                        BathroomImages bathroomImages = new BathroomImages(imageUri, userId_who_posted_img, bathroom_img_id);
                        arrayList_bathroomImg.add(bathroomImages);
                    }
                }
                photoAdapter1 = new PhotoAdapter1();
                hrzListView.setAdapter(photoAdapter1);
                // Set the emptyView to the ListView : http://www.myandroidsolutions.com/2014/09/25/listview-empty-message/
                hrzListView.setEmptyView(findViewById(R.id.emptyElement));

                /*
                    Onclick operation on the Image click
                    intent to the new activity.
                */
                hrzListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String Uri = arrayList_bathroomImg.get(position).getUri().toString();
                        userId_who_posted_img = arrayList_bathroomImg.get(position).getUser_id_posted_img();
                        bathroom_img_id = arrayList_bathroomImg.get(position).getBathroom_img_id();
                        //  Toast.makeText(getApplicationContext()," "+ arrayList_bathroomImg.get(position).getBathroom_img_id(),Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), FullImageViewActivity.class);
                        intent.putExtra("Image_Uri", Uri);
                        intent.putExtra("Bathroom_id", bathroom_id);
                        intent.putExtra("Bathroom_img_id", bathroom_img_id);
                        intent.putExtra("userId_who_posted_img", userId_who_posted_img);
                        startActivity(intent);
                    }
                });

            }
        });
    }
    View.OnClickListener mReportBathRoomClick1 = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            Boolean email_verify = currentUser.getBoolean("emailVerified");
            fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
            if ((!TextUtils.isNullOrEmpty(currentUser.getUsername()) && email_verify == true)|| fbUser) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        DetailBathRoomActivity.this);
                alertDialog.setMessage("Report as inappropriate?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("BathroomReport");
                        query1.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> list, ParseException e) {
                                int flag = 0;
                                for (int i = 0; i < list.size(); i++) {
                                    String get_user_id = list.get(i).getString("current_userId");
                                    String get_bathroom_id = list.get(i).getString("bathroomId");
                                    if (get_bathroom_id.equals(bathroom_id) && get_user_id.equals(user_Id)) {
                                        flag = 1;
                                    }
                                    if (flag == 1) {
                                        Toast.makeText(getApplicationContext(), "You have already added this as inappropriate Report", Toast.LENGTH_SHORT).show();

                                    } else {
                                        reportCount = 1;
                                        ParseObject parseObject = new ParseObject("BathroomReport");
                                        parseObject.put("bathroomId", bathroom_id);
                                        parseObject.put("reportCount", reportCount);
                                        parseObject.put("current_userId", ParseUser.getCurrentUser().getObjectId());
                                        parseObject.put("userInfo", ParseUser.getCurrentUser()); // Pointer of current user
                                        parseObject.put("bathroomInfo", ParseObject.createWithoutData("BathRoomDetail", bathroom_id));// pointer of bathroom detail
                                        parseObject.saveInBackground();
                                        //   ParseObject ratingByUser = new ParseObject("RattingByUser");
                                        final ParseQuery<ParseObject> query = ParseQuery.getQuery("BathRoomDetail");
                                        query.getInBackground(bathroom_id, new GetCallback<ParseObject>() {
                                            public void done(ParseObject object, ParseException e) {
                                                if (e == null) {
                                                    object.increment("ReportCount", 1);
                                                    object.saveInBackground();
                                                } else {
                                                    // something went wrong
                                                }
                                            }
                                        });
                                        Toast.makeText(getApplicationContext(), "Successfully added report", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
            else
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        DetailBathRoomActivity
                                .this);
                alertDialog.setMessage(getResources().getString(R.string.login_first_message));
                alertDialog.setPositiveButton("Login",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent in = new Intent(DetailBathRoomActivity.this, LoginActivity.class);
                                in.putExtra("BathType", "");
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
        }

    };
    @Override
    protected void onResume() {
        super.onResume();
        GetReview();
        // initialize pop up window
        popupWindow = showMenu();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                img_Menu.setImageResource(R.drawable.menu_icon);
            }
        });
    }


    public void displayData() {

        GetReview();
        new GetBathRoomImages().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void getImages() {
        new Picker.Builder(DetailBathRoomActivity.this, new MyPickListener(), R.style.MIP_theme)
                .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                .setLimit(10)
                .build()
                .startActivity();
    }

    private class MyPickListener implements Picker.PickListener {
        @Override
        public void onPickedSuccessfully(final ArrayList<ImageEntry> images) {
//            for (int i = 0; i < images.size(); i++) {
//                Log.d(TAG, "Image Path : " + images.get(i).path + "\n");
//            }
            UploadImage(images);
        }

        @Override
        public void onCancel() {
            // User cancled the pick activity
        }
    }

    private void UploadImage(ArrayList<ImageEntry> imageEntries) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        Log.e(TAG, " " + currentUser.getUsername());
        if (currentUser.getUsername() != null) {
            user_Id = currentUser.getObjectId();
            if (imageEntries != null) {
                for (int i = 0; i < imageEntries.size(); i++) {
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap photo = BitmapFactory.decodeFile(imageEntries.get(i).path, bmOptions);

                    Log.e(TAG, "Photo url" + imageEntries.get(i).path);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    // get byte array here
                    byte[] bytearray = stream.toByteArray();
                    // Create the ParseFile
                    long time = System.currentTimeMillis();
                    String sImageName = user_Id + bathroom_id + time + ".jpeg";
                    Log.d("Image Name : ", sImageName);

                    ParseFile file = new ParseFile(sImageName, bytearray);
                    // Upload the image into Parse Cloud
                    file.saveInBackground();
                    ParseObject parseObject = new ParseObject("BathroomImages");
                    // Create a column named "ImageName" and set the string
                    parseObject.put("bathroomID", bathroom_id);
                    parseObject.put("userId", user_Id);
                    // Create a column named "ImageFile" and insert the image
                    parseObject.put("bathroomImage", file);
                    parseObject.put("reportCount","0");
                    parseObject.put("approve", "YES");

                    Log.d(TAG, " " + bathroom_id + " " + user_Id);

                    // Create the class and the columns
                    parseObject.saveInBackground();

                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetailBathRoomActivity.this);
                // Setting Dialog Title
                alertDialog.setTitle("Thank you for Submission");
                // Setting Dialog Message
                alertDialog.setMessage("Photos are successfully added.");
                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                // Showing Alert Message
                alertDialog.show();

                new GetBathRoomImages().execute();

            }
        }
    }

    private void setUpMapIfNeeded(final double latitude, final double longitude) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                    .findFragmentById(R.id.map));
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            ViewGroup.LayoutParams params = mMapFragment.getView().getLayoutParams();
            params.height = height / 4;
            mMapFragment.getView().setLayoutParams(params);

            mMap.getUiSettings().setScrollGesturesEnabled(false);
            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude));
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.detail_bathroom_icon));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(16).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.addMarker(marker);
        }
    }

    public void setProgress(boolean visibility) {
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(DetailBathRoomActivity.this);
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

    public void GetReview() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("RattingByUser");
        query.whereMatches("bathRoomID", bathroom_id);
        query.addDescendingOrder("createdAt");
        query.addDescendingOrder("likeCount");
        Log.e("DetailBathRoomActivity", " " + bathroom_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    int total = list.size();
                    // for (int i = 0; (i < list.size()) && (i<review_show); i++) {
                    reviewListItems.clear();

                    for (int i = 0; i < list.size(); i++) {
                        //  total = list.size();
                        Log.e("DetailBathRoomActivity", list.size() + " ");

                        String username = list.get(i).getString("userName");
                        String descp = list.get(i).getString("MessageReview");
                        double rating = list.get(i).getDouble("bathRating");
                        String reviewUserId = list.get(i).getString("userId");
                        String review_id = list.get(i).getObjectId();
                        Log.e("Username", username + descp + rating + reviewUserId);
                        avg_rating += rating;
                        Log.d(TAG, " rating calculation " + rating);
                        ReviewListItem reviewListItem = new ReviewListItem(username, descp, rating, review_id, reviewUserId);

                        reviewListItems.add(reviewListItem);
                        Log.e("Size...", " " + reviewListItems.size());
                    }
                    reviewAdapter.notifyDataSetChanged();
                    Log.e("Size of array", "" + reviewListItems.size());

                   total = reviewListItems.size();
                    avg_rating = avg_rating / total;
                    Log.e(TAG, " avg rating " + (float) avg_rating);
                    //  String s = String.format("%.1f", String.valueOf(avg_rating));
                    //  float rate = Float.parseFloat(s);

                    ratingBathRoom.setRating((float) avg_rating);
                    avg_rating = 0.0;    // onStart and onResume both call.avg_rating call double.
                    txtReviewNumber.setText("" + total + " Review");
                    //}

                } else {
                    Log.e("Error", "");
                }
            }
        });
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        switch (index) {
            case 0:

                Log.e(TAG, " " + currentUser.getUsername());

                Log.d(TAG," "+email_verify);
                fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
                if ((!TextUtils.isNullOrEmpty(currentUser.getUsername()) && email_verify == true)|| fbUser) {
                    //  userName = currentUser.getUsername();
                    user_Id = currentUser.getObjectId();
                    final String rreview_id = reviewListItems.get(position).getReview_id();
                    final String review_user_id = reviewListItems.get(position).getReview_user_id();
                    Log.d(TAG, " review user id" + review_user_id);

                    // open

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            DetailBathRoomActivity.this);
                    // Setting Dialog Title
                    //  alertDialog.setTitle("Leave application?");
                    // Setting Dialog Message
                    alertDialog.setMessage("Report as inappropriate?");
                    // Setting Icon to Dialog
                    //alertDialog.setIcon(R.drawable.dialog_icon);
                    // Setting Positive "Yes" Button
                    alertDialog.setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ReportReview");
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> list, ParseException e) {
                                            int flag = 0;
                                            for (int i = 0; i < list.size(); i++) {
                                                String get_user_id = list.get(i).getString("reportedUser");
                                                String get_review_id = list.get(i).getString("reviewId");
                                                if (get_review_id.equals(rreview_id) && get_user_id.equals(user_Id)) {
                                                    flag = 1;
                                                }
                                            }
                                            if (flag == 1) {
                                                Toast.makeText(getApplicationContext(), "You have already added this as inappropriate Report", Toast.LENGTH_SHORT).show();
                                            } else {
                                                reportCount = 1;
                                                ParseObject parseObject = new ParseObject("ReportReview");
                                                parseObject.put("bathroomId", bathroom_id);
                                                parseObject.put("reviewId", rreview_id);
                                                parseObject.put("reviewUserId", review_user_id);   // who gave review
                                                parseObject.put("reportedUser", user_Id);  // Who login to account
                                                parseObject.put("reportCount", reportCount);
                                                parseObject.put("reportedUserInfo", ParseUser.getCurrentUser()); // Pointer of current user
                                                parseObject.put("bathInfo", ParseObject.createWithoutData("BathRoomDetail", bathroom_id));// pointer of bathroom detail
                                                parseObject.put("reviewInfo", ParseObject.createWithoutData("RattingByUser", rreview_id)); // pointer of RatingbyUser
                                                parseObject.saveInBackground();
                                                //   ParseObject ratingByUser = new ParseObject("RattingByUser");
                                                final ParseQuery<ParseObject> query = ParseQuery.getQuery("RattingByUser");
                                                query.getInBackground(rreview_id, new GetCallback<ParseObject>() {
                                                    public void done(ParseObject object, ParseException e) {
                                                        if (e == null) {
                                                            object.increment("reportCount", 1);
                                                            object.saveInBackground();
                                                        } else {
                                                            // something went wrong
                                                        }
                                                    }
                                                });
                                                Toast.makeText(getApplicationContext(), "Successfully added report", Toast.LENGTH_SHORT).show();


                                            }
                                        }
                                    });
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


                } else {
                    Log.e(TAG, "please login");
                    // /*
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            DetailBathRoomActivity
                                    .this);
                    alertDialog.setMessage(getResources().getString(R.string.login_first_message));
                    alertDialog.setPositiveButton("Login",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent in = new Intent(DetailBathRoomActivity.this, LoginActivity.class);
                                    in.putExtra("BathType", "");
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
                    //   alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.login_first_message));
                }


                break;

            case 1: {
                Log.e(TAG, " " + currentUser.getUsername());
                Log.d(TAG," "+email_verify);
                fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
                if ((!TextUtils.isNullOrEmpty(currentUser.getUsername()) && email_verify == true)|| fbUser)  {
                    //  userName = currentUser.getUsername();
                    user_Id = currentUser.getObjectId();
                    final String rreview_id = reviewListItems.get(position).getReview_id();

                    final String review_user_id = reviewListItems.get(position).getReview_user_id();

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("LikeReview");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            int flag = 0;
                            for (int i = 0; i < list.size(); i++) {
                                String get_user_id = list.get(i).getString("likeUser");
                                String get_review_id = list.get(i).getString("reviewId");
                                if (get_review_id.equals(rreview_id) && get_user_id.equals(user_Id)) {
                                    flag = 1;
                                }
                            }
                            if (flag == 1) {
                                Toast.makeText(getApplicationContext(), "You have already added", Toast.LENGTH_SHORT).show();
                            } else {
                                reviewLike = 1;
                                ParseObject parseObject = new ParseObject("LikeReview");
                                parseObject.put("bathroomId", bathroom_id);
                                parseObject.put("reviewId", rreview_id);
                                parseObject.put("likeUser", user_Id); // who login
                                parseObject.put("reviewUserId", review_user_id);
                                parseObject.put("likeCount", reviewLike);
                                parseObject.put("likeUserInfo", ParseUser.getCurrentUser()); // Pointer of current User
                                parseObject.put("bathInfo", ParseObject.createWithoutData("BathRoomDetail", bathroom_id));// pointer of bathroom detail
                                parseObject.put("reviewInfo", ParseObject.createWithoutData("RattingByUser", rreview_id)); // pointer of RatingbyUser

                                //   parseObject.saveInBackground();
                                parseObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        //   ParseObject ratingByUser = new ParseObject("RattingByUser");
                                        final ParseQuery<ParseObject> query = ParseQuery.getQuery("RattingByUser");
                                        query.getInBackground(rreview_id, new GetCallback<ParseObject>() {
                                            public void done(ParseObject object, ParseException e) {
                                                if (e == null) {
                                                    object.increment("likeCount", 1);
                                                    object.saveInBackground();
                                                } else {
                                                    // something went wrong
                                                }
                                            }
                                        });


                                    }
                                });
                                Toast.makeText(getApplicationContext(), "Successfully added", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                } else {
                    Log.e(TAG, "please login");
                    // /*
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            DetailBathRoomActivity
                                    .this);
                    // Setting Dialog Message
                    alertDialog.setMessage(getResources().getString(R.string.login_first_message));
                    // Setting Icon to Dialog
                    // Setting Positive "Yes" Button
                    alertDialog.setPositiveButton("Login",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //finish();
                                    // String bathDes= edtBathRoomDescrip.getText().toString();
                                    // float rating = rate_BathRoom.getRating();
                                    //   Log.d(TAG," detail on click on login "+BathRoomDescription+" "+rating+" "+BathRoomtype);

                                    Intent in = new Intent(DetailBathRoomActivity.this, LoginActivity.class);
                                    // Bundle bundle = new Bundle();
                                    //   in.putExtra("BathDescription",bathDes);
                                    //  in.putExtra("BathRating",rating);
                                    in.putExtra("BathType", "");
                                    // bundle.putString("BathDescription", bathDes);
                                    // bundle.putFloat("BathRating", rating);
                                    // bundle.putString("BathType", BathRoomtype);
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
                    //  alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.login_first_message));
                }

            }
            default:
                break;
        }

        return false;
    }



    class GetBathRoomImages extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress(true);

        }

        @Override
        protected Void doInBackground(Void... params) {
            showBathRoomImages();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setProgress(false);
        }
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

    Boolean fbUser=false;
    public PopupWindow showMenu() {
        //Initialize a pop up window type
        LayoutInflater inflater = (LayoutInflater) DetailBathRoomActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layoutt = new LinearLayout(this);
        PopupWindow popupWindow = new PopupWindow(layoutt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        Popup_Menu_Item menus[]; // = new Popup_Menu_Item[5];
        Boolean email_verify = currentUser.getBoolean("emailVerified");
        fbUser = ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
        Log.d("Splash screen "," "+email_verify);
        if ((currentUser.getUsername() != null && email_verify==true) || fbUser) {
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
        adapter = new PopupMenuAdapter(DetailBathRoomActivity.this, R.layout.popup_menu_item, menus);
        Log.e("After adapter", menus.length + " ");
        //the drop down list in a listview
        ListView lstMenu = new ListView(DetailBathRoomActivity.this);
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

        String TAG = "DetailBathRoomActivity.java";

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {

            // get the context and main activity to access variables
            Context mContext = v.getContext();
            DetailBathRoomActivity mainActivity = ((DetailBathRoomActivity) mContext);

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
                Intent intent = new Intent(getApplicationContext(),SearchLocationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
            } else if (data.equals(getString(R.string.search_near_me))) {
                if (Utils.isInternetConnected(DetailBathRoomActivity.this)) {
                    Intent intent = new Intent(getApplicationContext(), DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.connection_not_available));
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
                if (Utils.isInternetConnected(DetailBathRoomActivity.this)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetailBathRoomActivity.this);
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
                    alert.showAlertDialog(DetailBathRoomActivity.this, getResources().getString(R.string.connection_not_available));
                }
            } else if (data.equals(getResources().getString(R.string.Login_menu))) {
                //   flag_for_login = 1;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                in.putExtra("BathDescription", "");
                startActivity(in);
            }
        }

    }

    /*
       PhotoAdapter for Showing photos on the horizontal listview.
   */
    class PhotoAdapter1 extends BaseAdapter {

        private LayoutInflater inflater = null;
        private ViewHolder viewHolder;

        public PhotoAdapter1() {
            inflater = (LayoutInflater) DetailBathRoomActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return arrayList_bathroomImg.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList_bathroomImg.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            BathroomImages bean = (BathroomImages) getItem(position);
            try {
                viewHolder = new ViewHolder();
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_imageview, null);
                    viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.imageView);
                  //  viewHolder.ivCross = (ImageView) convertView.findViewById(R.id.ivCross);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                Ion.with(viewHolder.mImageView).load(String.valueOf(bean.getUri()));

                //viewHolder.mImageView.setImageURI(bean.getUri());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        public class ViewHolder {
            private ImageView mImageView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG," result code"+resultCode);
        if(resultCode==RESULT_OK)
        {
            Log.d(TAG," result code"+requestCode);
            if(requestCode==102)
            {
                String message=data.getStringExtra("MESSAGE");
                Log.d(TAG," message"+message);
                if(message.equalsIgnoreCase("YES")) {
                    Log.d(TAG," Show bath room");
                    new GetBathRoomImages().execute();
                }
            }
        }
    }
}
