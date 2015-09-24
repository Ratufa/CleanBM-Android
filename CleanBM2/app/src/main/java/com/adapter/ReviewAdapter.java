package com.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cleanbm.R;
import com.javabeans.ReviewListItem;

import java.util.List;

/**
 * Created by Ratufa.Paridhi on 8/21/2015.
 */
public class ReviewAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private static List<ReviewListItem> reviewListItem;
    private Context mContext;

    public ReviewAdapter(Context context, List<ReviewListItem> results) {
       // super(context, R.layout.rowlayout, names);
        this.mContext=context;
        this.reviewListItem = results;
       // mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return reviewListItem.size();
    }

    @Override
    public Object getItem(int position) {
        return reviewListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return reviewListItem.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        LayoutInflater mInflater = (LayoutInflater)
                mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

      //  Log.e("ReviewAdapter","position"+position+" "+(convertView==null));
        if (convertView == null) {
         //   mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.activity_review_row,null);
            holder = new ViewHolder();
          //  Log.e("Cstom adapter","sdkjhfsafhagsd");
            holder.txtUsername = (TextView) convertView.findViewById(R.id.txtUserName);
            holder.txtDecrp = (TextView) convertView.findViewById(R.id.txtReviewMssg);
            holder.ratingBar = (RatingBar)convertView.findViewById(R.id.review_rating);
            holder.ratingBar.setFocusable(false);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ReviewListItem rowItem = (ReviewListItem) getItem(position);

        holder.txtUsername.setText(rowItem.getUsername());
        holder.txtDecrp.setText(rowItem.getDescription());
        holder.ratingBar.setRating((float)rowItem.getRating());
       // holder.txtPhone.setText(searchArrayList.get(position).getPhone());
        return convertView;

    }

    static class ViewHolder {
        TextView txtUsername;
        TextView txtDecrp;
        RatingBar ratingBar;
       // TextView txtPhone;
    }
}
