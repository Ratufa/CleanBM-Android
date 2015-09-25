package com.cleanbm;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.Utils.Utils;
import com.koushikdutta.ion.Ion;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by Ratufa.Paridhi on 9/25/2015.
 */
public class FullImageViewActivity extends Activity
{
    ImageView img_fullImageView;
    Button btnInappropriateImg;
    String bathroom_id, bathroom_img_id,userId_who_posted_img,current_user_id;
    String TAG ="FullImageViewActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimageshow);
        img_fullImageView= (ImageView)findViewById(R.id.img_fullImageView);

        // Getting image uri from DetailBathroom activity and show on the Imageview
        String img_uri = getIntent().getStringExtra("Image_Uri");
        Ion.with(img_fullImageView).load(img_uri);

        btnInappropriateImg = (Button)findViewById(R.id.btnInappropriateImg);
        btnInappropriateImg.setOnClickListener(mButtonClickListener);

        bathroom_id = getIntent().getStringExtra("Bathroom_id");
        bathroom_img_id = getIntent().getStringExtra("Bathroom_img_id");
        userId_who_posted_img=getIntent().getStringExtra("userId_who_posted_img");
        current_user_id = ParseUser.getCurrentUser().getObjectId();
        Log.d(TAG," "+bathroom_id+" "+bathroom_img_id+" "+userId_who_posted_img+" "+current_user_id);

    }
    int flag = 0;
    View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.setProgress(FullImageViewActivity.this,true);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ReportImage");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {

                    for (int i = 0; i < list.size(); i++)
                    {
                        String get_user_id = list.get(i).getString("reportedUserId");
                        String get_image_id = list.get(i).getString("imageId");
                        if(get_image_id.equals(bathroom_img_id) && get_user_id.equals(current_user_id))
                        {
                            flag=1;
                        }
                    }

                    if(flag==1) {
                        Toast.makeText(getApplicationContext(), "You have already added this as inappropriate Image.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        ParseObject parseObject = new ParseObject("ReportImage");
                        parseObject.put("reportedUserInfo", ParseUser.getCurrentUser());
                        parseObject.put("bathInfo", ParseObject.createWithoutData("BathRoomDetail", bathroom_id));
                        parseObject.put("bathImagesInfo", ParseObject.createWithoutData("BathroomImages", bathroom_img_id));
                        parseObject.put("bathroomId", bathroom_id);
                        parseObject.put("reportedUserId", current_user_id);
                        parseObject.put("imageUserId", userId_who_posted_img);// id of person who posted that image
                        parseObject.put("imageId", bathroom_img_id);
                        parseObject.put("reportCount", 1);
                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                //   ParseObject ratingByUser = new ParseObject("RattingByUser");
                                final ParseQuery<ParseObject> query = ParseQuery.getQuery("BathroomImages");
                                query.getInBackground(bathroom_img_id, new GetCallback<ParseObject>() {
                                    public void done(ParseObject object, ParseException e) {
                                        if (e == null) {
                                            Log.d(TAG, "Log 11");
                                            object.increment("reportCount", 1);
                                            object.saveInBackground();
                                            Log.d(TAG, "Log 12");
                                        } else {
                                            // something went wrong
                                        }
                                    }
                                });


                            }
                        });

                    }
                }
            });
        Utils.setProgress(FullImageViewActivity.this,false);
        }
    };
}
