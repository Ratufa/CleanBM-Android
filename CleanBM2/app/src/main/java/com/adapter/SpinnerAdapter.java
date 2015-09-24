package com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cleanbm.R;
import com.javabeans.AddressBean;
import com.javabeans.PlacesSuggestionsBean;

import java.util.ArrayList;

/**
 * Created by Kailash on 16-Sep-15.
 */
public class SpinnerAdapter extends ArrayAdapter<PlacesSuggestionsBean> {

    ArrayList<PlacesSuggestionsBean> arrayList;

    public SpinnerAdapter(Context context, ArrayList<PlacesSuggestionsBean> list) {
        super(context, R.layout.spinner_item, list);
        this.arrayList = list;
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
