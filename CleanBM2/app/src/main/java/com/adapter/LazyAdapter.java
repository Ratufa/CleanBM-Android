package com.adapter;/*

package com.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cleanbm.R;
import com.javabeans.ReviewListItem;

public class LazyAdapter extends BaseAdapter {

    */
/*********** Declare Used Variables *********//*

    private Activity activity;
    private ArrayList<ReviewListItem> data;
    private static LayoutInflater inflater=null;
    public Resources res;
    ReviewListItem tempValues=null;
    int i=0;

    */
/*************  CustomAdapter Constructor *****************//*

    public LazyAdapter(Activity a, ArrayList<ReviewListItem> d,Resources resLocal) {

        */
/********** Take passed values **********//*

        activity = a;
        data=d;
        res = resLocal;

        */
/***********  Layout inflator to call external xml layout () ***********//*

        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    */
/******** What is the size of Passed Arraylist Size ************//*

    public int getCount() {

        if(data.size()<=0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    */
/********* Create a holder Class to contain inflated xml file elements *********//*

    public static class ViewHolder{

        TextView txtUsername;
        TextView txtDecrp;
        RatingBar ratingBar;

    }

    */
/****** Depends upon data size called for each row , Create each ListView row *****//*

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            */
/****** Inflate tabitem.xml file for each row ( Defined below ) *******//*

            vi = inflater.inflate(R.layout.activity_review_row, null);

            */
/****** View Holder Object to contain tabitem.xml file elements ******//*


            holder = new ViewHolder();
            holder.txtUsername = (TextView) convertView.findViewById(R.id.txtUserName);
            holder.txtDecrp = (TextView) convertView.findViewById(R.id.txtReviewMssg);
            holder.ratingBar = (RatingBar)convertView.findViewById(R.id.ratingBathRoom);

            */
/************  Set holder with LayoutInflater ************//*

            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
          //  holder.text.setText("No Data");

        }
        else
        {
            */
/***** Get each Model object from Arraylist ********//*

            tempValues=null;
            tempValues = ( ReviewListItem ) data.get( position );

            */
/************  Set Model values in Holder elements ***********//*


           */
/* holder.text.setText( tempValues.getCompanyName() );
            holder.text1.setText( tempValues.getUrl() );
            holder.image.setImageResource(
                    res.getIdentifier(
                            "com.androidexample.customlistview:drawable/" + tempValues.getImage()
                            , null, null));*//*


            holder.txtUsername.setText(data.get(position).getUsername());
            holder.txtDecrp.setText(data.get(position).getDescription());
            holder.ratingBar.setRating((float) data.get(position).getRating());

            */
/******** Set Item Click Listner for LayoutInflater for each row *******//*


         //   vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }
}
*/
