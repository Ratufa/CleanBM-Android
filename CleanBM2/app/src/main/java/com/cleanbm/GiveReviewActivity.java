package com.cleanbm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Utils.TextUtils;
import com.Utils.Utils;
import com.dialog.AlertDialogManager;
import com.javabeans.ImagesBean;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.widgets.HorizontalListView;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by Ratufa.Paridhi on 8/21/2015.
 */
public class GiveReviewActivity extends Activity {

    /*
        Images Variables
    */
    private ImageView ImageView_photo;
    int imageCount = 1;
    private ArrayList<ImagesBean> imagesList = new ArrayList<ImagesBean>();
    private HorizontalListView hrzListView;

    private ProgressDialog pd;
    RatingBar rate_BathRoom;
    TextView txtWriteurReview;
    RadioGroup radioGroup ; //,radioGrpLikeUnlike;
    String BathRoomtype="Squat";
    //String rating_value="0.0";
    String bathroom_id="";
    EditText edtReviewMsg;
   // int Like_unLike=2;
    String rating_value;
    static int flag=0;
    PopupWindow popupWindow;
    private ImageView img_Menu,img_Cancel,img_navigation_icon;
    String review_msg,userName,user_Id,full_address;
    String TAG ="GiveReviewActivity";
    TextView txt_Titlebar;

    private AlertDialogManager alert = new AlertDialogManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        bathroom_id = getIntent().getExtras().getString("bath_id");

        img_navigation_icon = (ImageView) findViewById(R.id.img_navigation_icon);
        img_navigation_icon.setImageResource(R.drawable.back_icon);
        img_navigation_icon.setVisibility(View.VISIBLE);
        img_navigation_icon.setOnClickListener(mMenuButtonClickListener);

        txt_Titlebar = (TextView)findViewById(R.id.txt_Titlebar);
        txt_Titlebar.setText("CleanBM Give Review");
        img_Menu =(ImageView)findViewById(R.id.navigation_icon);
        img_Menu.setVisibility(View.GONE);

        rate_BathRoom =(RatingBar)findViewById(R.id.ratingBathRoom);
        rate_BathRoom.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating_value = String.valueOf(ratingBar.getRating());
               }
        });

        radioGroup =(RadioGroup)findViewById(R.id.radioGroup);
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


        edtReviewMsg =(EditText)findViewById(R.id.edtReviewMsg);
        Utils.hideKeyBoard(getApplicationContext(), edtReviewMsg);

        txtWriteurReview =(TextView)findViewById(R.id.txt_GiveReview);
        txtWriteurReview.setOnClickListener(mButtonClickListener);


    }
    View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v == txtWriteurReview) {
                review_msg = edtReviewMsg.getText().toString().trim();
                if(review_msg.equals(""))
                {
                    alert.showAlertDialog(GiveReviewActivity.this,getResources().getString(R.string.enter_bath_description));
                }
                else if(TextUtils.isNullOrEmpty(rating_value))
                {
                    alert.showAlertDialog(GiveReviewActivity.this,"Please Give Rating");
                }
                else {
                    if (Utils.isInternetConnected(GiveReviewActivity.this)) {
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        Log.e(TAG, " " + currentUser.getUsername());
                       // if (currentUser.getUsername() != null) {
                            userName = currentUser.getString("name");
                            user_Id = currentUser.getObjectId();
                            HandlerAsync handlerAsync = new HandlerAsync();
                            handlerAsync.execute("", "", "");
                       // };
                    }
                    else
                    {
                        alert.showAlertDialog(GiveReviewActivity.this,getResources().getString(R.string.connection_not_available));
                    }
                }

            }
            else if (v == ImageView_photo) {
                new Picker.Builder(GiveReviewActivity.this, new MyPickListener(), R.style.MIP_theme)
                        .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                        .setLimit(10)
                        .build()
                        .startActivity();
            }
        }
    };

    private PhotoAdapter1 photoAdapter1;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }


    public class HandlerAsync extends AsyncTask<String,String ,String >
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {


            final ParseObject parseObject = new ParseObject("RattingByUser");
            parseObject.put("bathRating", Float.valueOf(rating_value));
            parseObject.put("MessageReview", review_msg);
            parseObject.put("bathRoomType", BathRoomtype);
            parseObject.put("userId", user_Id);
            parseObject.put("userName", userName);
            parseObject.put("bathRoomID", bathroom_id);
            parseObject.put("userInfo", ParseUser.getCurrentUser());
            ParseObject myBathRoomDetailPtr = ParseObject.createWithoutData("BathRoomDetail", parseObject.getObjectId());
            parseObject.put("bathInfo", myBathRoomDetailPtr);
          //  parseObject.put("bathRoomLike",Like_unLike);
            parseObject.saveInBackground();

            Log.e(TAG,rating_value+" "+review_msg+" "+BathRoomtype+"" +user_Id+" "+userName+" "+bathroom_id);
            // Upload the image into Parse Cloud
            // file.saveInBackground();

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
                        String sImageName = user_Id + bathroom_id + time + ".jpeg";
                        Log.d("Image Name : ", sImageName);

                        // Create the ParseFile
                        ParseFile file = new ParseFile(sImageName, bytearray);
                        // Upload the image into Parse Cloud
                        //.saveIn();
                        ParseObject parseObject1 = new ParseObject("BathroomImages");
                        // Create a column named "ImageName" and set the string
                        parseObject1.put("bathroomID", bathroom_id);
                        parseObject1.put("userId", user_Id);
                        // Create a column named "ImageFile" and insert the image
                        parseObject1.put("bathroomImage", file);
                        parseObject1.put("approve", "NO");
                        parseObject1.put("userInfo", ParseUser.getCurrentUser());
                        parseObject1.put("bathInfo", myBathRoomDetailPtr);

                        // Create the class and the columns
                        parseObject1.saveInBackground();
                        imageCount += 1;


                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setProgress(false);
            Toast.makeText(getApplicationContext(),"Successfully added and Photos are waiting for approval.",Toast.LENGTH_LONG).show();
            edtReviewMsg.setText("");
            rate_BathRoom.setRating(0);
        }
    }

    public void setProgress(boolean visibility) {
        if (visibility) {
            try {
                if (pd == null) {
                    pd = new ProgressDialog(GiveReviewActivity.this);
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


    View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == img_navigation_icon)
            {
              onBackPressed();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        flag=1;

    }

    class PhotoAdapter1 extends BaseAdapter {

        private LayoutInflater inflater = null;
        private ViewHolder viewHolder;

        public PhotoAdapter1() {
            inflater = (LayoutInflater) GiveReviewActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
